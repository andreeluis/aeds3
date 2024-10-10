package index.reversedList;

import index.IndexStrategy;
import java.util.*;

public interface ReversedListStrategy extends IndexStrategy {
    List<Long> getPositionByKey(String key);
    void addRegistryByKey(String key, long position);
}
