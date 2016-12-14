package de.metas.ui.web.window.model;

import java.util.ArrayList;
import java.util.List;

import org.adempiere.util.Check;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import de.metas.ui.web.window.datatypes.DocumentId;
import de.metas.ui.web.window.descriptor.DocumentEntityDescriptor;
import de.metas.ui.web.window.model.filters.DocumentFilter;

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

/**
 * Document query.
 * 
 * NOTE: this is not serializable, so please DO NOT CACHE IT
 * 
 * @author metas-dev <dev@metasfresh.com>
 *
 */
public final class DocumentQuery
{
	public static final Builder builder(final DocumentEntityDescriptor entityDescriptor)
	{
		return new Builder(entityDescriptor);
	}

	public static final DocumentQuery ofRecordId(final DocumentEntityDescriptor entityDescriptor, final int recordId)
	{
		Check.assumeNotNull(recordId, "Parameter recordId is not null");
		return builder(entityDescriptor).setRecordId(recordId).build();
	}

	private final DocumentsRepository documentsRepository;
	private final DocumentEntityDescriptor entityDescriptor;
	private final Integer recordId;
	private final Document parentDocument;

	private final List<DocumentFilter> filters;
	private final List<DocumentQueryOrderBy> orderBys;

	private final int firstRow;
	private final int pageLength;

	private DocumentQuery(final Builder builder)
	{
		super();
		documentsRepository = builder.getDocumentsRepository();
		entityDescriptor = builder.entityDescriptor; // not null
		recordId = builder.recordId;
		parentDocument = builder.parentDocument;

		filters = builder.filters == null ? ImmutableList.of() : ImmutableList.copyOf(builder.filters);
		orderBys = builder.orderBys == null ? ImmutableList.of() : ImmutableList.copyOf(builder.orderBys);

		firstRow = builder.firstRow;
		pageLength = builder.pageLength;
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this)
				.omitNullValues()
				.add("tableName", entityDescriptor.getTableName())
				.add("recordId", recordId)
				.add("parentDocument", parentDocument)
				.add("filters", filters.isEmpty() ? null : filters)
				.add("firstRow", firstRow > 0 ? firstRow : null)
				.add("pageLength", pageLength > 0 ? pageLength : null)
				.toString();
	}

	public Document retriveDocumentOrNull()
	{
		return documentsRepository.retrieveDocument(this);
	}
	
	public List<Document> retriveDocuments()
	{
		return documentsRepository.retrieveDocuments(this);
	}
	
	public DocumentEntityDescriptor getEntityDescriptor()
	{
		return entityDescriptor;
	}

	public Integer getRecordId()
	{
		return recordId;
	}

	public Document getParentDocument()
	{
		return parentDocument;
	}

	public Integer getParentLinkIdAsInt()
	{
		return parentDocument == null ? null : parentDocument.getDocumentIdAsInt();
	}

	public boolean isParentLinkIdSet()
	{
		return parentDocument != null;
	}

	public List<DocumentFilter> getFilters()
	{
		return filters;
	}

	public List<DocumentQueryOrderBy> getOrderBys()
	{
		return orderBys;
	}

	public int getFirstRow()
	{
		return firstRow;
	}

	public int getPageLength()
	{
		return pageLength;
	}

	public static final class Builder
	{
		private final DocumentEntityDescriptor entityDescriptor;
		private Document parentDocument;
		private Integer recordId;
		public List<DocumentFilter> filters = null;
		public List<DocumentQueryOrderBy> orderBys = null;
		private int firstRow = -1;
		private int pageLength = -1;

		private Builder(final DocumentEntityDescriptor entityDescriptor)
		{
			super();
			this.entityDescriptor = Preconditions.checkNotNull(entityDescriptor);
		}

		public DocumentQuery build()
		{
			return new DocumentQuery(this);
		}
		
		public Document retriveDocumentOrNull()
		{
			return build().retriveDocumentOrNull();
		}
		
		public List<Document> retriveDocuments()
		{
			return build().retriveDocuments();
		}


		private DocumentsRepository getDocumentsRepository()
		{
			return entityDescriptor.getDataBinding().getDocumentsRepository();
		}

		public Builder setRecordId(final Integer recordId)
		{
			this.recordId = recordId;
			return this;
		}

		public Builder setRecordId(final DocumentId documentId)
		{
			setRecordId(documentId.toInt());
			return this;
		}

		public Builder setParentDocument(final Document parentDocument)
		{
			this.parentDocument = parentDocument;
			return this;
		}

		public Builder addFilter(final DocumentFilter filter)
		{
			Check.assumeNotNull(filter, "Parameter filter is not null");

			if (filters == null)
			{
				filters = new ArrayList<>();
			}
			filters.add(filter);
			return this;
		}

		public Builder addFilters(final List<DocumentFilter> filtersToAdd)
		{
			if (filtersToAdd == null || filtersToAdd.isEmpty())
			{
				return this;
			}

			if (filters == null)
			{
				filters = new ArrayList<>();
			}
			filters.addAll(filtersToAdd);
			return this;
		}

		public Builder addOrderBy(final DocumentQueryOrderBy orderBy)
		{
			Check.assumeNotNull(orderBy, "Parameter orderBy is not null");

			if (orderBys == null)
			{
				orderBys = new ArrayList<>();
			}
			orderBys.add(orderBy);
			return this;
		}

		public Builder setFirstRow(final int firstRow)
		{
			this.firstRow = firstRow;
			return this;
		}

		public Builder setPageLength(final int pageLength)
		{
			this.pageLength = pageLength;
			return this;
		}
	}
}
