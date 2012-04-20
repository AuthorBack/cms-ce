package com.enonic.cms.core.search.index;

import java.util.Date;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Sets;

import com.enonic.cms.core.content.index.config.IndexFieldType;
import com.enonic.cms.core.search.builder.IndexFieldNameConstants;

public class ContentIndexDataElement
    extends IndexFieldNameConstants
{
    private String fieldBaseName;

    private final Set<Date> dateTimeValues = Sets.newHashSet();

    private final Set<Double> numericValues = Sets.newHashSet();

    private final Set<String> stringValues = Sets.newHashSet();

    private String orderBy;


    public ContentIndexDataElement( final String fieldBaseName, final Set<Object> values )
    {
        doSetValues( fieldBaseName, values );
    }

    private void doSetValues( String fieldBaseName, final Set<Object> values )
    {
        this.fieldBaseName = doNormalizeString( fieldBaseName );

        if ( values == null || values.isEmpty() )
        {
            return;
        }
        else
        {
            this.orderBy = ContentIndexOrderbyResolver.resolveOrderbyValue( values );
        }

        for ( final Object value : values )
        {
            if ( value == null )
            {
                continue;
            }

            stringValues.add( doNormalizeString( value.toString() ) );

            if ( value instanceof Number )
            {
                numericValues.add( ( (Number) value ).doubleValue() );
            }
            else if ( value instanceof Date )
            {
                dateTimeValues.add( (Date) value );
            }
            else
            {
                tryConvertValuesToValidTypes( value );
            }
        }
    }

    private void tryConvertValuesToValidTypes( final Object value )
    {
        final Double doubleValue = ContentIndexNumberValueResolver.resolveNumberValue( value );
        if ( doubleValue != null )
        {
            numericValues.add( doubleValue );
        }
        else
        {
            final Date dateValue = ContentIndexDateValueResolver.resolveDateValue( value );

            if ( dateValue != null )
            {
                dateTimeValues.add( dateValue );
            }
        }
    }


    private String doNormalizeString( final String stringValue )
    {
        return StringUtils.lowerCase( stringValue );
    }


    public Set<ContentIndexDataFieldValue> getAllFieldValuesForElement()
    {
        final Set<ContentIndexDataFieldValue> set = Sets.newHashSet();

        addStringFieldValue( set );
        addNumericFieldValue( set );
        addDateFieldValue( set );
        addSortFieldValue( set );

        return set;
    }

    private void addSortFieldValue( final Set<ContentIndexDataFieldValue> set )
    {
        if ( StringUtils.isNotBlank( this.orderBy ) )
        {
            set.add(
                new ContentIndexDataFieldValue( this.fieldBaseName + INDEX_FIELD_TYPE_SEPARATOR + ORDERBY_FIELDNAME_POSTFIX, orderBy ) );
        }
    }

    private void addStringFieldValue( final Set<ContentIndexDataFieldValue> set )
    {
        if ( stringValues != null && !stringValues.isEmpty() )
        {
            set.add( new ContentIndexDataFieldValue( this.fieldBaseName, stringValues ) );
        }
    }

    private void addDateFieldValue( final Set<ContentIndexDataFieldValue> set )
    {
        if ( dateTimeValues != null && !dateTimeValues.isEmpty() )
        {
            set.add( new ContentIndexDataFieldValue( this.fieldBaseName + INDEX_FIELD_TYPE_SEPARATOR + IndexFieldType.DATE.toString(),
                                                     dateTimeValues ) );
        }
    }

    private void addNumericFieldValue( final Set<ContentIndexDataFieldValue> set )
    {
        if ( numericValues != null && !numericValues.isEmpty() )
        {
            set.add( new ContentIndexDataFieldValue( this.fieldBaseName + INDEX_FIELD_TYPE_SEPARATOR + IndexFieldType.NUMBER.toString(),
                                                     numericValues ) );
        }
    }


}