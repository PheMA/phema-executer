package org.phema.executer.models.i2b2;

/**
 * Created by Luke Rasmussen on 1/18/18.
 */
public class QueryMaster implements Comparable<QueryMaster> {
    public long id;
    public String name;
    public long instanceId;
    public long count;

    public QueryMaster(long id, String name, long instanceId) {
        this.id = id;
        this.name = name;
        this.instanceId = instanceId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCount() { return count; }

    public void setCount(long count) { this.count = count; }

    public long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(long instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public int compareTo(QueryMaster other) {
        if (other == null) {
            return 1;
        }

        return Long.compare(this.getId(), other.getId());
    }
}
