package com.dbms.analyzer.repository;

import org.springframework.stereotype.Repository;
import com.dbms.analyzer.model.Relation;
import java.util.HashMap;
import java.util.Map;

@Repository
public class InMemorySessionRepository {

    private Map<String, Object> sessionData = new HashMap<>();

    /**
     * Saves data to session
     */
    public void save(String key, Object value) {
        sessionData.put(key, value);
    }

    /**
     * Retrieves data from session
     */
    public Object get(String key) {
        return sessionData.get(key);
    }

    /**
     * Removes data from session
     */
    public void remove(String key) {
        sessionData.remove(key);
    }

    /**
     * Clears all session data
     */
    public void clearAll() {
        sessionData.clear();
    }

    /**
     * Checks if key exists
     */
    public boolean containsKey(String key) {
        return sessionData.containsKey(key);
    }
}
