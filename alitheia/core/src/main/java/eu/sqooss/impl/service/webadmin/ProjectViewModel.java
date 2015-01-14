package eu.sqooss.impl.service.webadmin;

import eu.sqooss.service.db.Metric;
import eu.sqooss.service.pa.PluginInfo;

import java.util.Collection;
import java.util.List;

public class ProjectViewModel {

    public ProjectViewModel(boolean selected, long id, String name, String lastVersion, String lastCommit, String lastMail, String lastBug, String evalState, String nodename, Collection<PluginInfo> metrics) {
        this.selected = selected;
        this.id = id;
        this.name = name;
        this.lastVersion = lastVersion;
        this.lastMail = lastMail;
        this.lastBug = lastBug;
        this.evalState = evalState;
        this.nodename = nodename;
        this.metrics = metrics;
        this.lastCommit = lastCommit;
    }

    public boolean getSelected() {
        return selected;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLastVersion() {
        return lastVersion;
    }

    public String getLastCommit() {
        return lastCommit;
    }

    public String getLastMail() {
        return lastMail;
    }

    public String getLastBug() {
        return lastBug;
    }

    public String getEvalState() {
        return evalState;
    }

    public String getNodeName() {
        return nodename;
    }

    public Collection<PluginInfo> getMetrics() {
        return metrics;
    }

    boolean selected = false;
    long id = 0;
    String name = "";
    String lastVersion = "";
    String lastCommit = "";
    String lastMail = "";
    String lastBug = "";
    String evalState = "";
    String nodename = "(local)";

    Collection<PluginInfo> metrics;
}
