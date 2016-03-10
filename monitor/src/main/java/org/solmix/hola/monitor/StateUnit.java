package org.solmix.hola.monitor;

public class StateUnit
{
    private String application;

    private String group;

    private String operation;

    private String version;
    public StateUnit(MonitorState state){
        this.application=state.getApplication();
        this.group=state.getGroup();
        this.operation=state.getOperation();
        this.version=state.getVersion();
    }
    @Override
    public int hashCode(){
        int result = 17;
        if(application!=null)
        result = 37 * result + application.hashCode();
        if(group!=null)
        result = 37 * result + group.hashCode();
        if(operation!=null)
        result = 37 * result + operation.hashCode();
        if(version!=null)
        result = 37 * result + version.hashCode();
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StateUnit other = (StateUnit) obj;
        if (application == null) {
            if (other.application != null)
                return false;
        } else if (!application.equals(other.application))
            return false;
        if (group == null) {
            if (other.group != null)
                return false;
        } else if (!group.equals(other.group))
            return false;
        if (operation == null) {
            if (other.operation != null)
                return false;
        } else if (!operation.equals(other.operation))
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        return true;
    }
    
    public String getApplication() {
        return application;
    }
    
    public void setApplication(String application) {
        this.application = application;
    }
    
    public String getGroup() {
        return group;
    }
    
    public void setGroup(String group) {
        this.group = group;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public void setOperation(String operation) {
        this.operation = operation;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
}
