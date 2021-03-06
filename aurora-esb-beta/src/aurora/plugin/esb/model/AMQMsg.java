package aurora.plugin.esb.model;

import aurora.plugin.esb.model.xml.XMLHelper;

public class AMQMsg {

	private Task task;

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	static public String toXML(AMQMsg msg) {
		return XMLHelper.toCompositeMap(msg.getTask()).toXML();
		// return XMLHelper.toXML(msg.getTask());
	}

	static public AMQMsg toObject(String xml) {
		Task task = XMLHelper.toTask(xml);
		AMQMsg msg = new AMQMsg();
		msg.setTask(task);
		return msg;
	}
}
