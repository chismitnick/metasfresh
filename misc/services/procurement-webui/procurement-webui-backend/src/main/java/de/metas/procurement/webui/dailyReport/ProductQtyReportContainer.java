package de.metas.procurement.webui.dailyReport;

import de.metas.procurement.webui.model.Product;
import lombok.NonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/*
 * #%L
 * de.metas.procurement.webui
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
public final class ProductQtyReportContainer
{
	private Object autoSortPropertyId = ProductQtyReport.PROPERY_ProductName;

	public void setAutoSortByPropertyId(@NonNull final Object autoSortPropertyId)
	{
		if (Objects.equals(this.autoSortPropertyId, autoSortPropertyId))
		{
			return;
		}

		this.autoSortPropertyId = autoSortPropertyId;

		sort();
	}

	public Set<Product> getProducts()
	{
		final Set<Product> products = new HashSet<>();
		// for (final ProductQtyReport bean : getAllItemIds())
		// {
		// 	products.add(bean.getProduct());
		// }

		return products;
	}

	public void addAll(Collection<? extends ProductQtyReport> collection)
	{
		if (collection.isEmpty())
		{
			return;
		}

		// super.addAll(collection);
		sort();
	}

	public boolean hasProduct(final Product product)
	{
		return getByProduct(product) != null;
	}

	public ProductQtyReport getByProduct(final Product product)
	{
		if (product == null)
		{
			return null;
		}

		// for (final ProductQtyReport bean : getAllItemIds())
		// {
		// 	if (product.equals(bean.getProduct()))
		// 	{
		// 		return bean;
		// 	}
		// }

		return null;
	}

	// public ProductQtyReport getItemByProduct(final Product product)
	// {
	// 	if (product == null)
	// 	{
	// 		return null;
	// 	}
	//
	// 	for (final ProductQtyReport itemId : getAllItemIds())
	// 	{
	// 		if (product.equals(itemId.getProduct()))
	// 		{
	// 			return getItem(itemId);
	// 		}
	// 	}
	//
	// 	return null;
	// }

	public ProductQtyReport addBean(final ProductQtyReport bean)
	{
		// return super.addBean(bean);
		throw new UnsupportedOperationException();
	}

	// public final boolean addBeanItem(final ProductQtyReport item)
	// {
	// 	final ProductQtyReport newItemId = item.getBean();
	// 	final boolean filter = true;
	// 	final BeanItem<ProductQtyReport> newItem = internalAddItemAtEnd(newItemId, item, filter);
	// 	return newItem != null;
	// }

	public void sort()
	{
		// this.sort(new Object[] { autoSortPropertyId }, new boolean[] { true });
	}

	// public final ProductQtyReport removeProductIfZeroQty(final Product product)
	// {
	// 	final ProductQtyReport bean = getByProduct(product);
	// 	if (bean == null)
	// 	{
	// 		return null;
	// 	}
	//
	// 	if (bean.getQty().signum() != 0)
	// 	{
	// 		return null;
	// 	}
	//
	// 	final Object itemId = bean;
	// 	final BeanItem<ProductQtyReport> item = getItem(itemId);
	// 	if (!removeItem(itemId))
	// 	{
	// 		return null;
	// 	}
	//
	// 	return item;
	// }
}