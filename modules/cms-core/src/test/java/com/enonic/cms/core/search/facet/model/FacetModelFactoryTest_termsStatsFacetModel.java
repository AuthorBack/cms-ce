package com.enonic.cms.core.search.facet.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class FacetModelFactoryTest_termsStatsFacetModel
{
    private FacetsModelFactory facetsModelFactory = new FacetsModelFactory();

    @Test
    public void simple_model()
        throws Exception
    {
        String xml = "<facets>\n" +
            "    <terms-stats name=\"myFacetName\">\n" +
            "        <count>10</count>\n" +
            "        <key-index>data/activity</key-index>\n" +
            "        <value-index>data/hours</value-index>\n" +
            "        <orderby>count</orderby>\n" +
            "    </terms-stats>\n" +
            "</facets>";

        final FacetsModel facetsModel = facetsModelFactory.buildFromXml( xml );

        final FacetModel next = facetsModel.iterator().next();
        assertTrue( next instanceof TermsStatsFacetModel );

        TermsStatsFacetModel termsStatsFacetModel = (TermsStatsFacetModel) next;
        assertEquals( "data/activity", termsStatsFacetModel.getKeyIndex() );
        assertEquals( "data/hours", termsStatsFacetModel.getValueIndex() );
        assertEquals( "count", termsStatsFacetModel.getOrderby() );
    }


}
