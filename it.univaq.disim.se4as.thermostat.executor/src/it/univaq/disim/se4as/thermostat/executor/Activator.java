package it.univaq.disim.se4as.thermostat.executor;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import it.univaq.disim.se4as.thermostat.executorAPI.ExecutorAPI;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		
		ExecutorAPI executorAPI = new Executor(bundleContext);

		context.registerService(ExecutorAPI.class.getName(), executorAPI , null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
