package org.kmb.eventhub.dto;
import java.util.List;
import lombok.Data;

@Data
public class AddTagsToEventDTO {
    private Long eventId;
    private List<TagDTO> tags;
}
