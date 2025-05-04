package org.kmb.eventhub.tag.dto;
import java.util.List;

import lombok.Data;

@Data
public class EventTagsDTO {
    private List<TagDTO> tags;
}
