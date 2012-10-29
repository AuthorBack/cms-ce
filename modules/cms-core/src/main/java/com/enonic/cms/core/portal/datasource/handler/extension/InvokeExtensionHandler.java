package com.enonic.cms.core.portal.datasource.handler.extension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import com.enonic.cms.framework.util.JDOMUtil;

import com.enonic.cms.api.plugin.ext.FunctionLibrary;
import com.enonic.cms.core.plugin.ExtensionSet;
import com.enonic.cms.core.plugin.PluginManager;
import com.enonic.cms.core.portal.datasource.DataSourceException;
import com.enonic.cms.core.portal.datasource.handler.DataSourceRequest;
import com.enonic.cms.core.portal.datasource.handler.base.ParameterConverter;
import com.enonic.cms.core.portal.datasource.handler.base.SimpleDataSourceHandler;

@Component("ds.InvokeExtensionHandler")
public final class InvokeExtensionHandler
    extends SimpleDataSourceHandler
{
    private PluginManager pluginManager;

    private ParameterConverter parameterConverter;

    public InvokeExtensionHandler()
    {
        super( "invokeExtension" );
        parameterConverter = ParameterConverter.getInstance();
    }

    @Override
    public Document handle( final DataSourceRequest req )
        throws Exception
    {
        final String name = param( req, "name" ).required().asString();
        final String[] nameParts = name.split( "\\." );
        if ( nameParts.length != 2 )
        {
            throw new DataSourceException( "Extension [{0}] was not found", name );
        }
        final String namespace = nameParts[0];
        final String methodName = nameParts[1];

        final ExtensionSet extensions = pluginManager.getExtensions();
        final FunctionLibrary library = extensions.findFunctionLibrary( namespace );
        if ( library == null )
        {
            throw new DataSourceException( "Function library [{0}] was not found", namespace );
        }

        final String[] parameterValues = getParameterValues( req );
        final int parameterCount = parameterValues.length;
        final Method targetMethod = findMethod( library, methodName, parameterCount );
        if ( targetMethod == null )
        {
            throw new DataSourceException( "Method [{0}] with {1} parameters was not found in function library [{2}]", methodName,
                                           parameterCount == 0 ? "no" : parameterCount, namespace );
        }

        return wrapResultValue( invokeMethod( targetMethod, library.getTarget(), parameterValues ) );
    }

    private String[] getParameterValues( final DataSourceRequest req )
    {
        final List<String> parameterValues = Lists.newArrayList();
        final Map<String, String> params = req.getParams();
        int i = 1;
        while ( params.containsKey( "param" + i ) )
        {
            parameterValues.add( params.get( "param" + i ) );
            i++;
        }
        return parameterValues.toArray( new String[parameterValues.size()] );
    }

    private Object invokeMethod( final Method method, final Object target, final String... arguments )
        throws InvocationTargetException, IllegalAccessException
    {
        final Object[] methodArguments = convertArguments( arguments, method.getParameterTypes() );
        return method.invoke( target, methodArguments );
    }

    private Object[] convertArguments( final String[] arguments, final Class<?>[] parameterTypes )
    {
        final Object[] convertedArgs = new Object[arguments.length];
        for ( int i = 0; i < arguments.length; i++ )
        {
            convertedArgs[i] = parameterConverter.convert( arguments[i], parameterTypes[i] );
        }
        return convertedArgs;
    }

    private Document wrapResultValue( final Object value )
    {
        if ( value instanceof Document )
        {
            return (Document) value;
        }
        else if ( value instanceof org.w3c.dom.Document )
        {
            return JDOMUtil.toDocument( (org.w3c.dom.Document) value );
        }
        else
        {
            final Element root = new Element( "value" );
            if ( value != null )
            {
                root.setText( value.toString() );
            }
            return new Document( root );
        }
    }

    private Method findMethod( final FunctionLibrary library, final String methodName, final int parameterCount )
    {
        final Class<?> targetClass = library.getTargetClass();
        for ( Method method : targetClass.getMethods() )
        {
            if ( method.getName().equals( methodName ) && method.getParameterTypes().length == parameterCount )
            {
                return method;
            }
        }
        return null;
    }

    @Autowired
    public void setPluginManager( final PluginManager pluginManager )
    {
        this.pluginManager = pluginManager;
    }
}
