package org.example.db.neo4j;

import org.example.config.SnowFlakeHolder;
import org.neo4j.ogm.id.IdStrategy;

public class Neo4JCustomIdStrategy implements IdStrategy {
    @Override
    public Object generateId(Object o) {
        return Long.toString(SnowFlakeHolder.getSnowFlake().nextId());
    }
}
