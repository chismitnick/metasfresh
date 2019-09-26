package de.metas.handlingunits.material.interceptor;

import java.math.BigDecimal;

import org.adempiere.ad.modelvalidator.annotations.Interceptor;
import org.adempiere.ad.modelvalidator.annotations.ModelChange;
import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.ad.trx.api.ITrxManager;
import org.adempiere.ad.trx.api.OnTrxMissingPolicy;
import org.adempiere.mm.attributes.AttributeId;
import org.adempiere.mm.attributes.api.IAttributesBL;
import org.adempiere.model.InterfaceWrapperHelper;
import org.compiere.model.ModelValidator;
import org.springframework.stereotype.Component;

import de.metas.handlingunits.HuId;
import de.metas.handlingunits.model.I_M_HU_Attribute;
import de.metas.material.event.PostMaterialEventService;
import de.metas.util.Check;
import de.metas.util.Services;
import lombok.NonNull;

/*
 * #%L
 * de.metas.handlingunits.base
 * %%
 * Copyright (C) 2019 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

@Interceptor(I_M_HU_Attribute.class)
@Component
public class M_HU_Attribute
{
	private final IAttributesBL attributesService = Services.get(IAttributesBL.class);
	private final ITrxManager trxManager = Services.get(ITrxManager.class);
	private final PostMaterialEventService materialEventService;

	public M_HU_Attribute(@NonNull final PostMaterialEventService materialEventService)
	{
		this.materialEventService = materialEventService;
	}

	@ModelChange(timings = { ModelValidator.TYPE_AFTER_CHANGE }, ifColumnsChanged = {
			I_M_HU_Attribute.COLUMNNAME_Value,
			I_M_HU_Attribute.COLUMNNAME_ValueNumber,
			I_M_HU_Attribute.COLUMNNAME_ValueDate
	})
	public void onAttributeChanged(@NonNull final I_M_HU_Attribute record)
	{
		Check.assume(record.isActive(), "changing IsActive flag to false is not allowed: {}", record);

		final AttributeId attributeId = AttributeId.ofRepoId(record.getM_Attribute_ID());
		if (!attributesService.isStorageRelevant(attributeId))
		{
			return;
		}

		final HUAttributeChange change = extractHUAttributeChange(record);

		getOrCreateCollector().collect(change);
	}

	private HUAttributeChangesCollector getOrCreateCollector()
	{
		final ITrx trx = trxManager.getThreadInheritedTrx(OnTrxMissingPolicy.Fail); // at this point we always run in trx
		return trx.getPropertyAndProcessAfterCommit(
				HUAttributeChanges.class.getName(),
				() -> new HUAttributeChangesCollector(materialEventService),
				HUAttributeChangesCollector::createAndPostMaterialEvents);
	}

	private static HUAttributeChange extractHUAttributeChange(final I_M_HU_Attribute record)
	{
		final I_M_HU_Attribute recordBeforeChanges = InterfaceWrapperHelper.createOld(record, I_M_HU_Attribute.class);

		return HUAttributeChange.builder()
				.huId(HuId.ofRepoId(record.getM_HU_ID()))
				.attributeId(AttributeId.ofRepoId(record.getM_Attribute_ID()))
				//
				.valueString(record.getValue())
				.valueStringOld(recordBeforeChanges.getValue())
				//
				.valueNumber(extractValueNumberOrNull(record))
				.valueNumberOld(extractValueNumberOrNull(recordBeforeChanges))
				//
				.valueDate(record.getValueDate())
				.valueDateOld(recordBeforeChanges.getValueDate())
				//
				.build();
	}

	private static BigDecimal extractValueNumberOrNull(final I_M_HU_Attribute record)
	{
		return !InterfaceWrapperHelper.isNull(record, I_M_HU_Attribute.COLUMNNAME_ValueNumber)
				? record.getValueNumber()
				: null;
	}
}
