package com.emotibot.multiroundTemplateTest.conf;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.stereotype.Component;

import com.emotibot.middleware.conf.ConfigManager;
import com.emotibot.multiroundTemplateTest.constants.Constants;

@Component()
public class MyEmbeddedServletContainerFactory extends TomcatEmbeddedServletContainerFactory
{
    public EmbeddedServletContainer getEmbeddedServletContainer(ServletContextInitializer... initializers)  
    {  
        //设置端口  
        this.setPort(ConfigManager.INSTANCE.getPropertyInt(Constants.TOMCAT_PORT_KEY));
        return super.getEmbeddedServletContainer(initializers);  
    }
    
    @Override
    protected void customizeConnector(Connector connector)  
    {  
        super.customizeConnector(connector);  
        Http11NioProtocol protocol = (Http11NioProtocol)connector.getProtocolHandler();  
        //设置最大连接数  
        protocol.setMaxConnections(ConfigManager.INSTANCE.getPropertyInt(Constants.TOMCAT_MAX_CONNECTION_KEY));  
        //设置最大线程数  
        protocol.setMaxThreads(ConfigManager.INSTANCE.getPropertyInt(Constants.TOMCAT_MAX_THREAD_KEY));
        //连接超时数
        protocol.setConnectionTimeout(ConfigManager.INSTANCE.getPropertyInt(Constants.TOMCAT_CONNECTION_TIMEOUT_KEY));  
    }
}
