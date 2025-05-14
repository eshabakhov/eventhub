package org.kmb.eventhub.recommendation.mapper;

import org.kmb.eventhub.recommendation.dto.UserHistoryDTO;
import org.kmb.eventhub.tables.pojos.UserHistory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserHistoryMapper {
    UserHistoryDTO toDto(UserHistory userHistory);

    UserHistory dtoToUserHistory(UserHistoryDTO userHistoryDTO);

}
