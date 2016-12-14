package de.metas.ui.web.process;

import java.util.concurrent.ExecutionException;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.util.Services;
import org.compiere.model.I_AD_PInstance;
import org.compiere.util.Env;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.util.concurrent.UncheckedExecutionException;

import de.metas.process.IADPInstanceDAO;
import de.metas.process.ProcessInfo;
import de.metas.ui.web.process.descriptor.ProcessDescriptor;
import de.metas.ui.web.process.descriptor.ProcessDescriptorsFactory;
import de.metas.ui.web.process.descriptor.ProcessParametersRepository;
import de.metas.ui.web.window.descriptor.DocumentEntityDescriptor;
import de.metas.ui.web.window.model.Document;
import de.metas.ui.web.window.model.Document.CopyMode;

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

@Component
public class ProcessInstancesRepository
{
	//
	// Services
	@Autowired
	private ProcessDescriptorsFactory processDescriptorFactory;

	private final LoadingCache<Integer, ProcessInstance> processInstances = CacheBuilder.newBuilder()
			.removalListener(new RemovalListener<Integer, ProcessInstance>()
			{
				@Override
				public void onRemoval(final RemovalNotification<Integer, ProcessInstance> notification)
				{
					final ProcessInstance pinstance = notification.getValue();
					pinstance.destroy();
				}
			})
			.build(new CacheLoader<Integer, ProcessInstance>()
			{
				@Override
				public ProcessInstance load(final Integer pinstanceId)
				{
					return retrieveProcessInstance(pinstanceId);
				}
			});

	public void cacheReset()
	{
		processInstances.invalidateAll();
	}

	public ProcessDescriptor getProcessDescriptor(final int adProcessId)
	{
		return processDescriptorFactory.getProcessDescriptor(adProcessId);
	}

	public void checkin(final ProcessInstance processInstance)
	{
		processInstance.saveIfValidAndHasChanges(false); // throwEx=false
		processInstances.put(processInstance.getAD_PInstance_ID(), processInstance.copy(CopyMode.CheckInReadonly));
	}

	public ProcessInstance createNewProcessInstance(final ProcessInfo processInfo)
	{
		Services.get(IADPInstanceDAO.class).saveProcessInfoOnly(processInfo);
		final int adPInstanceId = processInfo.getAD_PInstance_ID();

		//
		// Build the parameters (as document)
		final int adProcessId = processInfo.getAD_Process_ID();
		final ProcessDescriptor processDescriptor = getProcessDescriptor(adProcessId);
		final DocumentEntityDescriptor parametersDescriptor = processDescriptor.getParametersDescriptor();
		final Document parametersDoc = ProcessParametersRepository.instance.createNewParametersDocument(parametersDescriptor, adPInstanceId);

		//
		final ProcessInstance pinstance = new ProcessInstance(processDescriptor, adPInstanceId, parametersDoc);
		processInstances.put(adPInstanceId, pinstance.copy(CopyMode.CheckInReadonly));
		return pinstance;
	}

	public ProcessInstance getProcessInstanceForReading(final int pinstanceId)
	{
		try
		{
			return processInstances.get(pinstanceId);
		}
		catch (final UncheckedExecutionException | ExecutionException e)
		{
			throw AdempiereException.wrapIfNeeded(e);
		}
	}

	public ProcessInstance getProcessInstanceForWriting(final int pinstanceId)
	{
		try
		{
			return processInstances.get(pinstanceId)
					.copy(CopyMode.CheckOutWritable);
		}
		catch (final UncheckedExecutionException | ExecutionException e)
		{
			throw AdempiereException.wrapIfNeeded(e);
		}
	}

	private ProcessInstance retrieveProcessInstance(final int adPInstanceId)
	{
		//
		// Get the ProcessDescriptor
		final I_AD_PInstance adPInstance = Services.get(IADPInstanceDAO.class).retrieveAD_PInstance(Env.getCtx(), adPInstanceId);
		final ProcessDescriptor processDescriptor = getProcessDescriptor(adPInstance.getAD_Process_ID());

		//
		// Build the parameters (as document)
		final DocumentEntityDescriptor parametersDescriptor = processDescriptor.getParametersDescriptor();
		final Document parametersDoc = parametersDescriptor
				.getDataBinding()
				.getDocumentsRepository()
				.retrieveDocumentById(parametersDescriptor, adPInstanceId);
		
		// TODO: handle the case when the process was already executed
		// In that case we need to load the result and provide it to ProcessInstance constructor

		return new ProcessInstance(processDescriptor, adPInstanceId, parametersDoc);
	}

}
