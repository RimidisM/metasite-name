package lt.metasite.task.app.domains;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class SortedResult {

    private String name;
    private Map<String, Long> result;
}
