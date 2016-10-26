package de.metas.ui.web.window.datatypes.json.filters;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.adempiere.util.GuavaCollectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import de.metas.ui.web.window.descriptor.filters.DocumentFilterDescriptor;
import de.metas.ui.web.window.descriptor.filters.DocumentFilterDescriptorsProvider;
import de.metas.ui.web.window.descriptor.filters.DocumentFilterParamDescriptor;
import de.metas.ui.web.window.model.filters.DocumentFilter;
import de.metas.ui.web.window.model.filters.DocumentFilterParam;

/*
 * #%L
 * metasfresh-webui-api
 * %%
 * Copyright (C) 2016 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

@SuppressWarnings("serial")
public class JSONDocumentFilter implements Serializable
{
	public static List<DocumentFilter> unwrapList(final List<JSONDocumentFilter> jsonFilters, final DocumentFilterDescriptorsProvider filterDescriptorProvider)
	{
		if (jsonFilters == null || jsonFilters.isEmpty())
		{
			return ImmutableList.of();
		}
		return jsonFilters
				.stream()
				.map(jsonFilter -> unwrap(jsonFilter, filterDescriptorProvider))
				.filter(filter -> filter != null)
				.collect(GuavaCollectors.toImmutableList());
	}

	public static final DocumentFilter unwrap(final JSONDocumentFilter jsonFilter, final DocumentFilterDescriptorsProvider filterDescriptorProvider)
	{
		final String filterId = jsonFilter.getFilterId();
		final DocumentFilterDescriptor filterDescriptor = filterDescriptorProvider.getByFilterIdOrNull(filterId);

		// Ad-hoc filters (e.g. zoom references)
		if (filterDescriptor == null)
		{
			return unwrapAsGenericFilter(jsonFilter);
		}
		// Filter with descriptor
		else
		{
			return unwrapUsingDescriptor(jsonFilter, filterDescriptor);
		}
	}

	private static DocumentFilter unwrapAsGenericFilter(final JSONDocumentFilter jsonFilter)
	{
		return DocumentFilter.builder()
				.setFilterId(jsonFilter.getFilterId())
				.setParameters(jsonFilter.getParameters()
						.stream()
						.map(jsonParam -> DocumentFilterParam.builder()
								.setFieldName(jsonParam.getParameterName())
								.setValue(jsonParam.getValue())
								.setValueTo(jsonParam.getValueTo())
								.setOperator()
								.build())
						.collect(GuavaCollectors.toImmutableList()))
				.build();
	}

	private static DocumentFilter unwrapUsingDescriptor(final JSONDocumentFilter jsonFilter, final DocumentFilterDescriptor filterDescriptor)
	{
		final DocumentFilter.Builder filter = DocumentFilter.builder()
				.setFilterId(jsonFilter.getFilterId());

		final Map<String, JSONDocumentFilterParam> jsonParams = Maps.uniqueIndex(jsonFilter.getParameters(), jsonParam -> jsonParam.getParameterName());

		for (final DocumentFilterParamDescriptor paramDescriptor : filterDescriptor.getParameters())
		{
			final String parameterName = paramDescriptor.getParameterName();
			final JSONDocumentFilterParam jsonParam = jsonParams.get(parameterName);
			
			// If parameter is missing: skip it if no required, else throw exception
			if (jsonParam == null)
			{
				if (paramDescriptor.isRequired())
				{
					throw new IllegalArgumentException("Parameter '" + parameterName + "' was not provided");
				}
				continue;
			}

			final Object value = jsonParam.getValue();
			final Object valueTo = jsonParam.getValueTo();

			// If there was no value/valueTo provided: skip it if no required, else throw exception
			if (value == null && valueTo == null)
			{
				if (paramDescriptor.isRequired())
				{
					throw new IllegalArgumentException("Parameter '" + parameterName + "' has no value");
				}
				continue;
			}

			filter.addParameter(DocumentFilterParam.builder()
					.setFieldName(paramDescriptor.getFieldName())
					.setOperator(paramDescriptor.getOperator())
					.setValue(value)
					.setValueTo(valueTo)
					.build());
		}

		for (final DocumentFilterParam internalParam : filterDescriptor.getInternalParameters())
		{
			filter.addParameter(internalParam);
		}

		return filter.build();

	}

	public static final JSONDocumentFilter of(final DocumentFilter filter)
	{
		final String filterId = filter.getFilterId();
		final List<JSONDocumentFilterParam> jsonParameters = filter.getParameters()
				.stream()
				.map(filterParam -> JSONDocumentFilterParam.of(filterParam))
				.collect(GuavaCollectors.toImmutableList());

		return new JSONDocumentFilter(filterId, jsonParameters);
	}

	@JsonProperty("filterId")
	private final String filterId;

	@JsonProperty("parameters")
	private final List<JSONDocumentFilterParam> parameters;

	@JsonCreator
	private JSONDocumentFilter(
			@JsonProperty("filterId") final String filterId //
			, @JsonProperty("parameters") final List<JSONDocumentFilterParam> parameters //
	)
	{
		super();
		this.filterId = filterId;
		this.parameters = parameters;
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this)
				.add("filterId", filterId)
				.add("parameters", parameters)
				.toString();
	}

	public String getFilterId()
	{
		return filterId;
	}

	public List<JSONDocumentFilterParam> getParameters()
	{
		return parameters;
	}
}
