/*
* Copyright 2016 herd contributors
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.finra.herd.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import org.finra.herd.dao.TagTypeDao;
import org.finra.herd.dao.config.DaoSpringModuleConfig;
import org.finra.herd.model.AlreadyExistsException;
import org.finra.herd.model.api.xml.TagType;
import org.finra.herd.model.api.xml.TagTypeCreateRequest;
import org.finra.herd.model.api.xml.TagTypeKey;
import org.finra.herd.model.api.xml.TagTypeKeys;
import org.finra.herd.model.api.xml.TagTypeUpdateRequest;
import org.finra.herd.model.jpa.TagTypeEntity;
import org.finra.herd.service.TagTypeService;
import org.finra.herd.service.helper.AlternateKeyHelper;

/**
 * The tag type service implementation.
 */
@Service
@Transactional(value = DaoSpringModuleConfig.HERD_TRANSACTION_MANAGER_BEAN_NAME)
public class TagTypeServiceImpl implements TagTypeService
{
    @Autowired
    private AlternateKeyHelper alternateKeyHelper;

    @Autowired
    private TagTypeDao tagTypeDao;

    @Override
    public TagType createTagType(TagTypeCreateRequest tagTypeCreateRequest)
    {
        // Validate and trim the request parameters.
        validateTagTypeCreateRequest(tagTypeCreateRequest);

        // Get the tag type key.
        TagTypeKey tagTypeKey = tagTypeCreateRequest.getTagTypeKey();

        // Validate the tag type does not already exist in the database.
        TagTypeEntity tagTypeEntity = tagTypeDao.getTagTypeByKey(tagTypeKey);
        if (tagTypeEntity != null)
        {
            throw new AlreadyExistsException(
                String.format("Unable to create tag type with code \"%s\" because it already exists.", tagTypeKey.getTagTypeCode()));
        }

        // Create and persist a new tag type entity from the request information.
        tagTypeEntity = createTagTypeEntity(tagTypeKey.getTagTypeCode(), tagTypeCreateRequest.getDisplayName(), tagTypeCreateRequest.getTagTypeOrder());

        // Create and return the tag type object from the persisted entity.
        return createTagTypeFromEntity(tagTypeEntity);
    }

    @Override
    public TagType updateTagType(TagTypeKey tagTypeKey, TagTypeUpdateRequest tagTypeUpdateRequest)
    {
        return null;
    }

    @Override
    public TagType getTagType(TagTypeKey tagTypeKey)
    {
        return null;
    }

    @Override
    public TagType deleteTagType(TagTypeKey tagTypeKey)
    {
        return null;
    }

    @Override
    public TagTypeKeys getTagTypes()
    {
        return null;
    }

    /**
     * Validates the tag type create request. This method also trims the request parameters.
     *
     * @param request the tag type create request
     */
    private void validateTagTypeCreateRequest(TagTypeCreateRequest request)
    {
        Assert.notNull(request, "A storage policy create request must be specified.");

        validateTagTypeKey(request.getTagTypeKey());

        // Validate display name
        Assert.hasText(request.getDisplayName(), "A storage policy status must be specified.");
        request.setDisplayName(request.getDisplayName().trim());
    }

    /**
     * Validates the tag type key. This method also trims the key parameters.
     *
     * @param key the tag type key
     *
     * @throws IllegalArgumentException if any validation errors were found
     */
    public void validateTagTypeKey(TagTypeKey key) throws IllegalArgumentException
    {
        Assert.notNull(key, "A tag type key must be specified.");
        key.setTagTypeCode(alternateKeyHelper.validateStringParameter("tag type code", key.getTagTypeCode()));
    }

    /**
     * Creates and persists a new tag type entity.
     *
     * @param tagTypeCode the tag type code
     * @param displayName the display name
     * @param tagTypeOrder the tag type order number
     *
     * @return the newly created tag type entity
     */
    private TagTypeEntity createTagTypeEntity(String tagTypeCode, String displayName, int tagTypeOrder)
    {
        TagTypeEntity tagTypeEntity = new TagTypeEntity();

        tagTypeEntity.setTypeCode(tagTypeCode);
        tagTypeEntity.setDisplayName(displayName);
        tagTypeEntity.setOrderNumber(tagTypeOrder);

        return tagTypeDao.saveAndRefresh(tagTypeEntity);
    }

    /**
     * Creates the tag type registration from the persisted entity.
     *
     * @param tagTypeEntity the tag type registration entity
     *
     * @return the tag type registration
     */
    private TagType createTagTypeFromEntity(TagTypeEntity tagTypeEntity)
    {
        TagType tagType = new TagType();

        tagType.setId(tagTypeEntity.getId());

        TagTypeKey tagTypeKey = new TagTypeKey();
        tagType.setTagTypeKey(tagTypeKey);
        tagTypeKey.setTagTypeCode(tagTypeEntity.getTypeCode());

        tagType.setDisplayName(tagTypeEntity.getDisplayName());
        tagType.setTagTypeOrder(tagTypeEntity.getOrderNumber());

        return tagType;
    }
}
