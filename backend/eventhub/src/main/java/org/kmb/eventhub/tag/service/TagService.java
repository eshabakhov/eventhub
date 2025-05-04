package org.kmb.eventhub.tag.service;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.kmb.eventhub.common.dto.ResponseList;
import org.kmb.eventhub.event.exception.EventNotFoundException;
import org.kmb.eventhub.user.exception.UserNotFoundException;
import org.kmb.eventhub.tag.exception.TagNotFoundException;
import org.kmb.eventhub.tag.mapper.TagMapper;
import org.kmb.eventhub.tables.daos.EventDao;
import org.kmb.eventhub.tables.daos.TagDao;
import org.kmb.eventhub.tables.pojos.Tag;
import org.kmb.eventhub.tag.repository.TagRepository;
import org.kmb.eventhub.tag.dto.TagDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.jooq.impl.DSL.trueCondition;

@Service
@AllArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    private final TagDao tagDao;

    private final EventDao eventDao;

    private final TagMapper tagMapper;

    public ResponseList<Tag> getList(Integer page, Integer pageSize) {
        ResponseList<Tag> responseList = new ResponseList<>();
        Condition condition = trueCondition();

        List<Tag> list =  tagRepository.fetch(condition, page, pageSize);

        responseList.setList(list);
        responseList.setTotal(tagRepository.count(condition));
        responseList.setCurrentPage(page);
        responseList.setPageSize(pageSize);
        return responseList;
    }

    @Transactional
    public Tag create(TagDTO tagDTO) {
        Tag tag = tagMapper.toEntity(tagDTO);
        tagDao.insert(tag);
        return tag;
    }

    public Tag get(Long id) {
        return tagDao.fetchOptionalById(id).orElseThrow(() -> new TagNotFoundException(id));
    }

    @Transactional
    public Long deleteTagFromEvent(Long eventId, TagDTO tagDTO) {
        Long tagId = tagDTO.getId();
        if (tagDao.fetchOptionalById(tagId).isEmpty()) {
            throw new TagNotFoundException(tagId);
        }
        eventDao.fetchOptionalById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        tagRepository.deleteTagFromEvent(tagId, eventId);
        if (!tagRepository.tagIsUsed(tagId)) {
            tagDao.deleteById(tagId);
        }
        return tagId;
    }

    @Transactional
    public Long deleteTagFromUser(Long userId, TagDTO tagDTO) {
        Long tagId = tagDTO.getId();
        if (tagDao.fetchOptionalById(tagId).isEmpty()) {
            throw new TagNotFoundException(tagId);
        }
        eventDao.fetchOptionalById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        tagRepository.deleteTagFromUser(tagId, userId);
        if (tagRepository.tagIsUsed(tagId)) {
            tagDao.deleteById(tagId);
        }
        return tagId;
    }

    public List<Tag> checkAllTags(List<Tag> tagList) {
        List<Tag> result = new ArrayList<>();
        tagList.forEach(tag-> {
            Tag existingTag = tagRepository.findTagByName(tag.getName());
            if (Objects.isNull(existingTag)) {
                result.add(tagRepository.createTag(tag.getName()));
            }
            else {
                result.add(existingTag);
            }
        });
        return result;
    }

    @Transactional
    public List<Tag> addTagsToEvent(Long eventId, List<TagDTO> tagNamesDTO) {
        List<Tag> tagNames = tagNamesDTO.stream().map(tagMapper::toEntity).toList();
        eventDao.findOptionalById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        //1. Добавление новых тегов, получение id всех тегов из запроса
        List<Tag> tagWithId = checkAllTags(tagNames);

        //2. Получение списка использованных тегов для мероприятия
        Set<Long> usedTagIds = getUsedTagIdsForEvent(eventId);

        //3. Получение id неиспользованных тегов
        List<Tag> newTags = tagWithId.stream()
                .filter(tag -> !usedTagIds.contains(tag.getId()))
                .toList();

        //4. Добавление связи для новых тегов и мероприятия
        if (!newTags.isEmpty()) {
            assignTagsToEvent(eventId, newTags);
        }

        return newTags;
    }

    public Set<Long> getUsedTagIdsForEvent(Long eventId) {
        return tagRepository.getUsedTagIdsForEvent(eventId);
    }

    public void assignTagsToEvent(Long eventId, List<Tag> tags) {
         tagRepository.assignNewEventTag(eventId,tags);
    }

    public Set<Long> getUsedTagIdsForUser(Long userId) {
        return tagRepository.getUsedTagIdsForUser(userId);
    }

    public void assignTagsToUser(Long userId, List<Tag> tags) {
        tagRepository.assignNewUserTag(userId,tags);
    }
}
