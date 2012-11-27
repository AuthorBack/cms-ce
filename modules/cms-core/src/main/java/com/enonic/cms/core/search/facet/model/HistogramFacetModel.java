package com.enonic.cms.core.search.facet.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.google.common.base.Strings;

@XmlAccessorType(XmlAccessType.NONE)
public class HistogramFacetModel
    extends AbstractFacetModel
{
    private String index;

    private Long interval;

    private String keyIndex;

    private String valueIndex;

    @XmlElement(name = "index")
    public String getIndex()
    {
        return index;
    }

    public void setIndex( final String index )
    {
        this.index = index;
    }

    @XmlElement(name = "interval")
    public long getInterval()
    {
        return interval;
    }

    public void setInterval( final long interval )
    {
        this.interval = interval;
    }

    @XmlElement(name = "key-index")
    public String getKeyIndex()
    {
        return keyIndex;
    }

    public void setKeyIndex( final String keyIndex )
    {
        this.keyIndex = keyIndex;
    }

    @XmlElement(name = "value-index")
    public String getValueIndex()
    {
        return valueIndex;
    }

    public void setValueIndex( final String valueIndex )
    {
        this.valueIndex = valueIndex;
    }

    @Override
    public void validate()
    {
        super.validate();

        if ( Strings.isNullOrEmpty( this.index ) && Strings.isNullOrEmpty( this.keyIndex ) )
        {
            throw new IllegalArgumentException( "Error in histogram-facet  " + getName() + ": 'field' or 'key-index' must be set" );
        }

        if ( Strings.isNullOrEmpty( this.index ) && !Strings.isNullOrEmpty( this.keyIndex ) && Strings.isNullOrEmpty( this.valueIndex ) )
        {
            throw new IllegalArgumentException(
                "Error in histogram-facet  " + getName() + ": both 'key-index' and 'value-index' must be set" );
        }

        if ( this.interval == null )
        {
            throw new IllegalArgumentException( "Error in histogram-facet " + getName() + ": 'interval' must be set" );
        }

    }


}

