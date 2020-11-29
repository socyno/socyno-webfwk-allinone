package org.socyno.webfwk.util.remote;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.lang.management.MemoryUsage;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.lang3.ArrayUtils;
import org.socyno.webfwk.util.tool.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JmxClient {
      
    final private int port;
    final private String host;
    private JMXConnector connector = null;
    private MBeanServerConnection mbsc = null;
    private final static String JMX_URL =
            "service:jmx:rmi:///jndi/rmi://%s:%s/jmxrmi";
    @SuppressWarnings("serial")
    private final static Map<String, String[]> JMX_PROPERTIES = new HashMap<String, String[]>() { {
        put("jre", new String[] {
            "JMImplementation:type=MBeanServerDelegate",
            "ImplementationVendor", "ImplementationVersion"
        } );
        put("tomcat", new String[] {
            "Catalina:type=Server",
            "serverInfo", "port"
        } );
        put("engine", new String[] {
            "Catalina:type=Engine",
            "baseDir", "jvmRoute",
            "name"
        } );
        put("threading", new String[] {
            "java.lang:type=Threading",
            "PeakThreadCount", "DaemonThreadCount",
            "ThreadCount", "TotalStartedThreadCount"
        } );
        put("host", new String[] {
            "java.lang:type=OperatingSystem",
            "Name", "Arch", "TotalPhysicalMemorySize",
            "MaxFileDescriptorCount", "FreePhysicalMemorySize",
            "ProcessCpuTime", "TotalSwapSpaceSize",
            "OpenFileDescriptorCount", "FreeSwapSpaceSize",
            "AvailableProcessors", "CommittedVirtualMemorySize",
            "Version", "SystemLoadAverage", "SystemCpuLoad", 
            "ProcessCpuLoad", "AvailableProcessors"
        } );
        put("threadPool", new String[] {
            "Catalina:type=Executor,name=tomcatThreadPool",
            "completedTaskCount", "maxQueueSize",
            "maxIdleTime", "maxThreads", "minSpareThreads",
            "largestPoolSize", "corePoolSize", "poolSize",
            "queueSize", "activeCount"
        } );
        put("memory", new String[] {
            "java.lang:type=Memory",
            "HeapMemoryUsage", "NonHeapMemoryUsage"
        } );
        put("runtime", new String[] {
            "java.lang:type=Runtime",
            "StartTime", "Uptime",
            "ClassPath", "InputArguments"
        } );
        put("webModules", new String[] {
            "Catalina:type=Host,host=localhost",
            "children"
        } );
        put("__webModule", new String[] {
            null,
            "path", "startTime", 
            "stateName", "state"
        } );
        put("c3p0Tokens", new String[] {
            "com.mchange.v2.c3p0:type=C3P0Registry",
             "AllIdentityTokens"
        } );
        put("__c3p0DataSource", new String[] {
            "com.mchange.v2.c3p0:type=PooledDataSource[%s]",
            "dataSourceName",
            "driverClass", "jdbcUrl", "user", "maxPoolSize", 
            "numBusyConnections", "numConnections", "numIdleConnections"    
        } );
    } };
    
    public JmxClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    @SuppressWarnings("serial")
    public void connect() throws IOException {
        if (connector != null) {
            try {
                connector.getConnectionId();
            } catch (IOException e) {
                close();
                connector = null;
            }
        }
        if (connector == null) {
            JMXServiceURL url = new JMXServiceURL(String.format(JMX_URL, host, port));
            connector = JMXConnectorFactory.connect(url, new HashMap<String, Object>() {
                {
                    put("jmx.remote.credentials", new String[] { "monitorRole", "QED" });
                }
            });
            log.info(String.format("Tomcat JMX connected : %s", url));
        }
        mbsc = connector.getMBeanServerConnection();
    }
    
    public void close() {
        try {
            connector.close();
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public List<Map<String, String>> getDataSourceInfo()
            throws MalformedObjectNameException, ReflectionException, IOException {
        Object tokens;
        Map<String, Object> info = getInfo("c3p0Tokens");
        if ( info == null || (tokens=info.get("AllIdentityTokens")) == null ) {
            return null;
        }
        Map<String, String> entry;
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        for ( String token : (String[])tokens ) {
            String[] args = (String[])ArrayUtils.clone(
                    JMX_PROPERTIES.get("__c3p0DataSource"));
            if ((entry=simpleObject(getInfo(String.format(args[0], token), args))) != null
                   && entry.containsKey("dataSourceName")  ) {
                result.add(entry);
            }
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, String> getHostInfo() 
            throws MalformedObjectNameException, 
            ReflectionException,
            IOException {
        return simpleObject(getInfo("host"));
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, String> getJreInfo() 
            throws MalformedObjectNameException, 
            ReflectionException,
            IOException {
        return simpleObject(getInfo("jre"));
    }

    public Map<String,MemoryUsage> getMemoryInfo() 
            throws MalformedObjectNameException, 
            ReflectionException, 
            IOException {
        Map<String, Object> info;
        if ( (info=getInfo("memory")) == null ) {
            return null;
        }
        Map<String,MemoryUsage> memory = new HashMap<String,MemoryUsage>();
        for ( Map.Entry<String, Object> m : info.entrySet() ) {
            memory.put(m.getKey(), MemoryUsage.from(((CompositeDataSupport)m.getValue())));
        }
        return memory;
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, String> getRuntimeInfo() 
            throws MalformedObjectNameException, 
            ReflectionException,
            IOException {
        return simpleObject(getInfo("runtime"));
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, String> getThreadingInfo() 
            throws MalformedObjectNameException, 
            ReflectionException,
            IOException {
        return simpleObject(getInfo("threading"));
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, String> getThreadPoolInfo() 
            throws MalformedObjectNameException, 
            ReflectionException,
            IOException {
        return simpleObject(getInfo("threadPool"));
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, String> getTomcatInfo() 
            throws MalformedObjectNameException, 
            ReflectionException,
            IOException {
        return simpleObject(getInfo("tomcat"), getInfo("engine"));
    }
    
    @SuppressWarnings("unchecked")
    public Map<String,Map<String, String>> getWebModuleInfo() 
            throws MalformedObjectNameException, 
            ReflectionException, 
            IOException {
        Map<String, Object> info = getInfo("webModules");
        if ( info == null || !info.containsKey("children") ) {
            return null;
        }
        Map<String, String> m;
        Map<String,Map<String, String>> modules =
                new HashMap<String,Map<String, String>>();
        for ( ObjectName name : (ObjectName[])info.get("children") ) {

            String[] args = (String[])ArrayUtils.clone(
                    JMX_PROPERTIES.get("__webModule"));
            if ( (m=simpleObject(getInfo(name, args))) != null ) {
                modules.put(m.get("path"), m);
            }
        }
        return modules;
    }
    
    private Map<String, Object> getInfo(ObjectName objName, String[] fields)
            throws ReflectionException, IOException {
        if ( objName == null || fields == null ) {
            return null;
        }
        AttributeList attributes = null;
        try {
            attributes = mbsc.getAttributes(
                    objName, fields);
        } catch(InstanceNotFoundException e) {
            return null;
        }
        Map<String, Object> result = new HashMap<String, Object>();
        for (Object a : attributes) {
            result.put(((Attribute)a).getName(), ((Attribute)a).getValue());
        }
        return result;
    }
    
     private Map<String, Object> getInfo(String objName, String[] fields)
             throws MalformedObjectNameException, 
                 ReflectionException, 
                 IOException {
         return getInfo(new ObjectName(objName), fields);
     }
    
    private Map<String, Object> getInfo(String property)
            throws MalformedObjectNameException, ReflectionException, IOException {
        String[] args;
        if ((args = JMX_PROPERTIES.get(property)) == null) {
            return null;
        }
        return getInfo(args[0], args);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> simpleObject(Map<String, Object>... maps) {
        return simpleObject(" ", maps);
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, String> simpleObject(String sep, Map<String, Object>... maps) {
        if (maps == null || maps.length == 0) {
            return null;
        }
        Object v;
        Map<String, String> result = new HashMap<String, String>();
        for (Map<String, Object> m : maps) {
            if (m == null) {
                continue;
            }
            for (Map.Entry<String, Object> e : m.entrySet()) {
                if ((v = e.getValue()) == null) {
                    continue;
                }
                String s = v.toString();
                if (v instanceof String[]) {
                    s = StringUtils.join((String[]) v, sep);
                }
                result.put(e.getKey(), s);
            }
        }
        return result;
    }
}
