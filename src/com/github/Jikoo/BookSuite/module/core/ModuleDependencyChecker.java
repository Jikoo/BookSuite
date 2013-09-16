package com.github.Jikoo.BookSuite.module.core;

import java.util.LinkedList;
import java.util.List;

public class ModuleDependencyChecker {
	
	private boolean dependenciesMet = false;
	private boolean dependenciesChanged = true;
	private ModuleManagementSystem mms;
	private List<String> dependencies = new LinkedList<String>();
	
	public boolean dependenciesFulfilled()
	{
		if (mms.depenancyUpdated || this.dependenciesChanged)
		{
			dependenciesMet = true;
			for(String s : this.dependencies)
			{
				BookSuiteModule bsm = mms.getModuleByName(s);
				dependenciesMet &= (bsm!=null && bsm.isEnabled());
			}
		}
		this.dependenciesChanged = false;
		return this.dependenciesMet;
	}
	
	public void addDependency(String s)
	{
		this.dependenciesChanged = true;
		this.dependencies.add(s);
	}
}
