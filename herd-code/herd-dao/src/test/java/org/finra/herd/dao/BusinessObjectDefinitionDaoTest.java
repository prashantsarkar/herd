/*
* Copyright 2015 herd contributors
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
package org.finra.herd.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import org.finra.herd.model.api.xml.BusinessObjectDefinitionKey;
import org.finra.herd.model.jpa.BusinessObjectDefinitionEntity;
import org.finra.herd.model.jpa.TagEntity;

public class BusinessObjectDefinitionDaoTest extends AbstractDaoTest
{
    @Test
    public void testGetBusinessObjectDefinitionByKey()
    {
        // Create two business object definitions having the same business object definition name.
        BusinessObjectDefinitionEntity businessObjectDefinitionEntity =
            businessObjectDefinitionDaoTestHelper.createBusinessObjectDefinitionEntity(NAMESPACE, BDEF_NAME, DATA_PROVIDER_NAME, BDEF_DESCRIPTION);
        businessObjectDefinitionDaoTestHelper.createBusinessObjectDefinitionEntity(NAMESPACE_2, BDEF_NAME, DATA_PROVIDER_NAME, BDEF_DESCRIPTION);

        // Get the business object definition by key.
        BusinessObjectDefinitionEntity resultBusinessObjectDefinitionEntity =
            businessObjectDefinitionDao.getBusinessObjectDefinitionByKey(new BusinessObjectDefinitionKey(NAMESPACE, BDEF_NAME));

        // Validate the returned object.
        assertEquals(businessObjectDefinitionEntity, resultBusinessObjectDefinitionEntity);
    }

    @Test
    public void testGetBusinessObjectDefinitionKeys()
    {
        // Create and persist business object definition entities.
        for (BusinessObjectDefinitionKey key : businessObjectDefinitionDaoTestHelper.getTestBusinessObjectDefinitionKeys())
        {
            businessObjectDefinitionDaoTestHelper.createBusinessObjectDefinitionEntity(key, DATA_PROVIDER_NAME, BDEF_DESCRIPTION);
        }

        // Retrieve a list of business object definition keys.
        assertEquals(businessObjectDefinitionDaoTestHelper.getExpectedBusinessObjectDefinitionKeys(),
            businessObjectDefinitionDao.getBusinessObjectDefinitionKeys());
    }

    @Test
    public void testGetBusinessObjectDefinitionKeysByNamespace()
    {
        // Create and persist business object definition entities.
        for (BusinessObjectDefinitionKey key : businessObjectDefinitionDaoTestHelper.getTestBusinessObjectDefinitionKeys())
        {
            businessObjectDefinitionDaoTestHelper.createBusinessObjectDefinitionEntity(key, DATA_PROVIDER_NAME, BDEF_DESCRIPTION);
        }

        // Retrieve a list of business object definition keys for the specified namespace.
        assertEquals(businessObjectDefinitionDaoTestHelper.getExpectedBusinessObjectDefinitionKeysForNamespace(),
            businessObjectDefinitionDao.getBusinessObjectDefinitionKeysByNamespace(NAMESPACE));

        // Retrieve a list of business object definition keys without specifying a namespace.
        assertEquals(businessObjectDefinitionDaoTestHelper.getExpectedBusinessObjectDefinitionKeys(),
            businessObjectDefinitionDao.getBusinessObjectDefinitionKeysByNamespace(BLANK_TEXT));

        // Retrieve a list of business object definition keys for the specified namespace.
        assertEquals(businessObjectDefinitionDaoTestHelper.getExpectedBusinessObjectDefinitionKeysForNamespace(),
            businessObjectDefinitionDao.getBusinessObjectDefinitionKeysByNamespace(NAMESPACE));

        // Retrieve a list of business object definition keys for the specified namespace in uppercase.
        assertEquals(businessObjectDefinitionDaoTestHelper.getExpectedBusinessObjectDefinitionKeysForNamespace(),
            businessObjectDefinitionDao.getBusinessObjectDefinitionKeysByNamespace(NAMESPACE.toUpperCase()));

        // Retrieve a list of business object definition keys for the specified namespace in lowercase.
        assertEquals(businessObjectDefinitionDaoTestHelper.getExpectedBusinessObjectDefinitionKeysForNamespace(),
            businessObjectDefinitionDao.getBusinessObjectDefinitionKeysByNamespace(NAMESPACE.toLowerCase()));

        // Try to retrieve a list of business object definition keys for a non-existing namespace.
        assertTrue(businessObjectDefinitionDao.getBusinessObjectDefinitionKeysByNamespace("I_DO_NOT_EXIST").isEmpty());
    }

    @Test
    public void testGetBusinessObjectDefinitions()
    {
        // Create and persist two business object definition entities.
        List<BusinessObjectDefinitionEntity> businessObjectDefinitionEntities =
            businessObjectDefinitionDaoTestHelper.createExpectedBusinessObjectDefinitionEntities();

        // Get the list of business object definitions when tag entities is empty.
        assertEquals(ImmutableSet.copyOf(businessObjectDefinitionEntities),
            ImmutableSet.copyOf(businessObjectDefinitionDao.getBusinessObjectDefinitions(new ArrayList<>())));

        // Create and persist root tag entity.
        TagEntity parentTagEntity = tagDaoTestHelper.createTagEntity(TAG_TYPE, TAG_CODE, TAG_DISPLAY_NAME_2, TAG_DESCRIPTION, null);

        // Create two children for the root tag.
        List<TagEntity> tagEntities = Arrays
            .asList(parentTagEntity, tagDaoTestHelper.createTagEntity(TAG_TYPE, TAG_CODE_3, TAG_DISPLAY_NAME_4, TAG_DESCRIPTION, parentTagEntity),
                tagDaoTestHelper.createTagEntity(TAG_TYPE, TAG_CODE_4, TAG_DISPLAY_NAME_3, TAG_DESCRIPTION, parentTagEntity));

        // Create and persist two business object definition tag entities for the child tag entities.
        for (BusinessObjectDefinitionEntity businessObjectDefinitionEntity : businessObjectDefinitionEntities)
        {
            for (TagEntity tagEntity : tagEntities)
            {
                businessObjectDefinitionTagDaoTestHelper.createBusinessObjectDefinitionTagEntity(businessObjectDefinitionEntity, tagEntity);
            }
        }

        // Filter duplicates and validate the result.
        assertEquals(ImmutableSet.copyOf(businessObjectDefinitionEntities),
            ImmutableSet.copyOf(businessObjectDefinitionDao.getBusinessObjectDefinitions(tagEntities)));
    }
}