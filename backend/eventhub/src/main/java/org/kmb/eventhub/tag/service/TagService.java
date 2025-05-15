package org.kmb.eventhub.tag.service;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.kmb.eventhub.common.dto.ResponseList;
import org.kmb.eventhub.event.exception.EventNotFoundException;
import org.kmb.eventhub.tables.daos.UserDao;
import org.kmb.eventhub.tag.dto.EventTagsDTO;
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

import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.trueCondition;

@Service
@AllArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    private final TagDao tagDao;

    private final UserDao userDao;

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

    public Tag get(Long id) {
        return tagDao.fetchOptionalById(id).orElseThrow(() -> new TagNotFoundException(id));
    }

    @Transactional
    public void addTagsToUser(Long tagId, Long userId) {
        userDao.findOptionalById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        tagDao.findOptionalById(tagId).orElseThrow(() -> new TagNotFoundException(tagId));

        assignTagsToUser(userId, tagId);
    }

    @Transactional
    public List<Tag> addTagsToEvent(Long eventId, EventTagsDTO eventTagsDTO) {

        eventDao.findOptionalById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        List<String> tagNames = eventTagsDTO.getTags().stream().map(TagDTO::getName).toList();

        List<String> newTagNames = createNewTags(tagNames);
        List<Tag> newTags = tagRepository.fetch(newTagNames);

        assignTagsToEvent(eventId, newTags);
        return newTags;
    }

    @Transactional
    public void deleteTagFromEvent(Long tagId, Long eventId) {
        if (tagDao.fetchOptionalById(tagId).isEmpty()) {
            throw new TagNotFoundException(tagId);
        }
        eventDao.fetchOptionalById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        tagRepository.deleteTagFromEvent(tagId, eventId);
        if (!tagRepository.tagIsUsed(tagId)) {
            tagDao.deleteById(tagId);
        }
    }

    @Transactional
    public void deleteTagFromUser(Long tagId, Long userId) {
        if (tagDao.fetchOptionalById(tagId).isEmpty()) {
            throw new TagNotFoundException(tagId);
        }
        eventDao.fetchOptionalById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        tagRepository.deleteTagFromUser(tagId, userId);
        if (!tagRepository.tagIsUsed(tagId)) {
            tagDao.deleteById(tagId);
        }
    }

    public List<String> createNewTags(List<String> tags) {
        List<String> existingTagNames = tagRepository.fetch(tags).stream().map(Tag::getName).toList();

        List<String> newTagNames = tags.stream()
                .filter(tag -> !existingTagNames.contains(tag))
                .distinct()
                .toList();

        tagRepository.createTags(newTagNames);

        return newTagNames;
    }

    public Set<Long> getUsedTagIdsForEvent(Long eventId) {
        return tagRepository.getUsedTagIdsForEvent(eventId);
    }

    public void assignTagsToEvent(Long eventId, List<Tag> tags) {
         tagRepository.assignNewEventTag(eventId, tags);
    }

    public Set<Long> getUsedTagIdsForUser(Long userId) {
        return tagRepository.getUsedTagIdsForUser(userId);
    }

    public void assignTagsToUser(Long userId, Long tagId) {
        tagRepository.assignTagToUser(tagId, userId);
    }
}
