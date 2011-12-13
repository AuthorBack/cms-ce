package com.enonic.cms.core.search.query;

import com.enonic.cms.core.content.index.ContentIndexQuery;
import com.enonic.cms.core.content.index.queryexpression.*;
import com.enonic.cms.core.search.ElasticContentConstants;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;


public final class QueryTranslator {

    public SearchSourceBuilder build(ContentIndexQuery query)
            throws Exception {
        final SearchSourceBuilder builder = new SearchSourceBuilder();

        builder.from(query.getIndex());

        builder.size(query.getCount());

        final QueryExpr expr = QueryParser.newInstance().parse(query.getQuery());
        builder.query(buildExpr(expr.getExpr()));
        OrderQueryBuilder.buildOrderByExpr(builder, expr.getOrderBy());
        FilterQueryBuilder.buildFilterQuery(builder, query);

        System.out.println( "****************************\n\r" + builder.toString() + "\n\r\n\r" );

        return builder;
    }

    private QueryBuilder buildExpr(Expression expr)
            throws Exception {

        if (expr == null) {
            return QueryBuilders.matchAllQuery();
        }

        if (expr instanceof CompareExpr) {
            return buildCompareExpr((CompareExpr) expr);
        }

        if (expr instanceof LogicalExpr) {
            return buildLogicalExpr((LogicalExpr) expr);
        }

        if (expr instanceof NotExpr) {
            return buildNotExpr((NotExpr) expr);
        }

        throw new RuntimeException(expr.getClass().getName() + " expression not supported");
    }


    private QueryBuilder buildCompareExpr(CompareExpr expr) {

        final int operator = expr.getOperator();
        final String path = QueryFieldNameResolver.toFieldName((FieldExpr) expr.getLeft());

        final QueryPath queryPath = QueryPathCreator.createQueryPath(path);

        final Object[] values = QueryValueResolver.toValues(expr.getRight());
        final Object singleValue = values.length > 0 ? values[0] : null;

        switch (operator) {
            case CompareExpr.EQ:
                return TermQueryBuilderCreator.buildTermQuery(queryPath, singleValue);
            case CompareExpr.NEQ:
                return buildNotQuery(TermQueryBuilderCreator.buildTermQuery(queryPath, singleValue));
            case CompareExpr.GT:
                return RangeQueryBuilder.buildRangeQuery(path, singleValue, null, false, true);
            case CompareExpr.GTE:
                return RangeQueryBuilder.buildRangeQuery(path, singleValue, null, true, true);
            case CompareExpr.LT:
                return RangeQueryBuilder.buildRangeQuery(path, null, singleValue, true, false);
            case CompareExpr.LTE:
                return RangeQueryBuilder.buildRangeQuery(path, null, singleValue, true, true);
            case CompareExpr.LIKE:
                return buildLikeQuery(path, (String) singleValue);
            case CompareExpr.NOT_LIKE:
                return buildNotQuery(buildLikeQuery(path, (String) singleValue));
            case CompareExpr.IN:
                return buildInQuery(path, values);
            case CompareExpr.NOT_IN:
                return buildNotQuery(buildInQuery(path, values));
            case CompareExpr.FT:
                return buildFulltextQuery(path, singleValue);
        }

        return null;
    }

    private QueryBuilder buildFulltextQuery(final String path, final Object singleValue) {
        String stringValue = (String) singleValue;
        return QueryBuilders.termQuery(path + ElasticContentConstants.NON_ANALYZED_POSTFIX, stringValue);
    }

    private QueryBuilder buildNotExpr(NotExpr expr)
            throws Exception {
        final QueryBuilder negated = buildExpr(expr.getExpr());
        return buildNotQuery(negated);
    }

    private QueryBuilder buildInQuery(String field, Object[] values) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        for (Object value : values) {
            boolQuery.should(QueryBuilders.termQuery(field, value));
        }

        return boolQuery;
    }

    private QueryBuilder buildLikeQuery(String field, String value) {
        return QueryBuilders.wildcardQuery(field, StringUtils.replaceChars(value, '%', '*'));
    }

    private QueryBuilder buildLogicalExpr(LogicalExpr expr)
            throws Exception {
        final QueryBuilder left = buildExpr(expr.getLeft());
        final QueryBuilder right = buildExpr(expr.getRight());

        if (expr.getOperator() == LogicalExpr.OR) {
            return QueryBuilders.boolQuery().should(left).should(right);
        } else if (expr.getOperator() == LogicalExpr.AND) {
            return QueryBuilders.boolQuery().must(left).must(right);
        } else {
            throw new IllegalArgumentException("Operation [" + expr.getToken() + "] not supported");
        }
    }

    private QueryBuilder buildNotQuery(QueryBuilder negated) {
        return QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery()).mustNot(negated);
    }

}
