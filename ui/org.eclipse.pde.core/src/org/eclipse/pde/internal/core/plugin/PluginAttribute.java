package org.eclipse.pde.internal.core.plugin;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.PrintWriter;
import java.util.Hashtable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.model.ConfigurationPropertyModel;
import org.eclipse.pde.core.ISourceObject;
import org.eclipse.pde.core.plugin.IPluginAttribute;
import org.eclipse.pde.internal.core.ischema.*;
import org.w3c.dom.Node;

public class PluginAttribute extends PluginObject implements IPluginAttribute {
	private String value;
	private transient ISchemaAttribute attributeInfo;

public PluginAttribute() {
}
PluginAttribute(IPluginAttribute attribute) {
	setModel(attribute.getModel());
	setParent(attribute.getParent());
	this.name = attribute.getName();
	this.value = attribute.getValue();
	this.attributeInfo = ((PluginAttribute)attribute).getAttributeInfo();
}
public Object clone() {
	return new PluginAttribute(this);
}
public ISchemaAttribute getAttributeInfo() {
	if (attributeInfo!=null) {
		ISchema schema = attributeInfo.getSchema();
		if (schema.isDisposed()) {
			attributeInfo = null;
		}
	}
	if (attributeInfo==null) {
		PluginElement element = (PluginElement)getParent();
		ISchemaElement elementInfo = element.getElementInfo();
		if (elementInfo!=null) {
			attributeInfo = elementInfo.getAttribute(getName());
		}
	}
	return attributeInfo;
}
public String getValue() {
	return value;
}
void load(ConfigurationPropertyModel attributeModel) {
	this.name = attributeModel.getName();
	this.value = attributeModel.getValue();
}
void load(Node node, Hashtable lineTable) {
	this.name = node.getNodeName();
	this.value = node.getNodeValue();
	if (getParent() instanceof ISourceObject) {
		ISourceObject pobj = (ISourceObject)getParent();
		int start = pobj.getStartLine();
		int stop = pobj.getStopLine();
		if (start!= -1 && stop!= -1) {
			range = new int[] { start, stop };
		}
	}
}

public void setAttributeInfo(ISchemaAttribute newAttributeInfo) {
	attributeInfo = newAttributeInfo;
}
public void setValue(String newValue) throws CoreException {
	ensureModelEditable();
	String oldValue = this.value;
	this.value = newValue;
	AttributeChangedEvent e = new AttributeChangedEvent(getParent(), this, oldValue, newValue);
	fireModelChanged(e);
}

public void write(String indent, PrintWriter writer) {
	if (value==null) return;
	writer.print(indent);
	writer.print(getName()+"=\""+getWritableString(value)+"\"");
}
}
