// This file was generated by Mendix Studio Pro.
//
// WARNING: Code you write here will be lost the next time you deploy the project.

package opcuaclient_exampleimplementation.proxies;

public class ReferenceTypeId
{
	private final com.mendix.systemwideinterfaces.core.IMendixObject referenceTypeIdMendixObject;

	private final com.mendix.systemwideinterfaces.core.IContext context;

	/**
	 * Internal name of this entity
	 */
	public static final java.lang.String entityName = "OpcUaClient_ExampleImplementation.ReferenceTypeId";

	/**
	 * Enum describing members of this entity
	 */
	public enum MemberNames
	{
		NamespaceIndex("NamespaceIndex"),
		Identifier("Identifier"),
		_type("_type"),
		_null("_null"),
		NotNull("NotNull"),
		ReferenceTypeId_OpcUaNode("OpcUaClient_ExampleImplementation.ReferenceTypeId_OpcUaNode");

		private java.lang.String metaName;

		MemberNames(java.lang.String s)
		{
			metaName = s;
		}

		@java.lang.Override
		public java.lang.String toString()
		{
			return metaName;
		}
	}

	public ReferenceTypeId(com.mendix.systemwideinterfaces.core.IContext context)
	{
		this(context, com.mendix.core.Core.instantiate(context, "OpcUaClient_ExampleImplementation.ReferenceTypeId"));
	}

	protected ReferenceTypeId(com.mendix.systemwideinterfaces.core.IContext context, com.mendix.systemwideinterfaces.core.IMendixObject referenceTypeIdMendixObject)
	{
		if (referenceTypeIdMendixObject == null)
			throw new java.lang.IllegalArgumentException("The given object cannot be null.");
		if (!com.mendix.core.Core.isSubClassOf("OpcUaClient_ExampleImplementation.ReferenceTypeId", referenceTypeIdMendixObject.getType()))
			throw new java.lang.IllegalArgumentException("The given object is not a OpcUaClient_ExampleImplementation.ReferenceTypeId");

		this.referenceTypeIdMendixObject = referenceTypeIdMendixObject;
		this.context = context;
	}

	/**
	 * @deprecated Use 'ReferenceTypeId.load(IContext, IMendixIdentifier)' instead.
	 */
	@java.lang.Deprecated
	public static opcuaclient_exampleimplementation.proxies.ReferenceTypeId initialize(com.mendix.systemwideinterfaces.core.IContext context, com.mendix.systemwideinterfaces.core.IMendixIdentifier mendixIdentifier) throws com.mendix.core.CoreException
	{
		return opcuaclient_exampleimplementation.proxies.ReferenceTypeId.load(context, mendixIdentifier);
	}

	/**
	 * Initialize a proxy using context (recommended). This context will be used for security checking when the get- and set-methods without context parameters are called.
	 * The get- and set-methods with context parameter should be used when for instance sudo access is necessary (IContext.createSudoClone() can be used to obtain sudo access).
	 */
	public static opcuaclient_exampleimplementation.proxies.ReferenceTypeId initialize(com.mendix.systemwideinterfaces.core.IContext context, com.mendix.systemwideinterfaces.core.IMendixObject mendixObject)
	{
		return new opcuaclient_exampleimplementation.proxies.ReferenceTypeId(context, mendixObject);
	}

	public static opcuaclient_exampleimplementation.proxies.ReferenceTypeId load(com.mendix.systemwideinterfaces.core.IContext context, com.mendix.systemwideinterfaces.core.IMendixIdentifier mendixIdentifier) throws com.mendix.core.CoreException
	{
		com.mendix.systemwideinterfaces.core.IMendixObject mendixObject = com.mendix.core.Core.retrieveId(context, mendixIdentifier);
		return opcuaclient_exampleimplementation.proxies.ReferenceTypeId.initialize(context, mendixObject);
	}

	public static java.util.List<opcuaclient_exampleimplementation.proxies.ReferenceTypeId> load(com.mendix.systemwideinterfaces.core.IContext context, java.lang.String xpathConstraint) throws com.mendix.core.CoreException
	{
		java.util.List<opcuaclient_exampleimplementation.proxies.ReferenceTypeId> result = new java.util.ArrayList<opcuaclient_exampleimplementation.proxies.ReferenceTypeId>();
		for (com.mendix.systemwideinterfaces.core.IMendixObject obj : com.mendix.core.Core.retrieveXPathQuery(context, "//OpcUaClient_ExampleImplementation.ReferenceTypeId" + xpathConstraint))
			result.add(opcuaclient_exampleimplementation.proxies.ReferenceTypeId.initialize(context, obj));
		return result;
	}

	/**
	 * Commit the changes made on this proxy object.
	 */
	public final void commit() throws com.mendix.core.CoreException
	{
		com.mendix.core.Core.commit(context, getMendixObject());
	}

	/**
	 * Commit the changes made on this proxy object using the specified context.
	 */
	public final void commit(com.mendix.systemwideinterfaces.core.IContext context) throws com.mendix.core.CoreException
	{
		com.mendix.core.Core.commit(context, getMendixObject());
	}

	/**
	 * Delete the object.
	 */
	public final void delete()
	{
		com.mendix.core.Core.delete(context, getMendixObject());
	}

	/**
	 * Delete the object using the specified context.
	 */
	public final void delete(com.mendix.systemwideinterfaces.core.IContext context)
	{
		com.mendix.core.Core.delete(context, getMendixObject());
	}
	/**
	 * @return value of NamespaceIndex
	 */
	public final java.lang.Integer getNamespaceIndex()
	{
		return getNamespaceIndex(getContext());
	}

	/**
	 * @param context
	 * @return value of NamespaceIndex
	 */
	public final java.lang.Integer getNamespaceIndex(com.mendix.systemwideinterfaces.core.IContext context)
	{
		return (java.lang.Integer) getMendixObject().getValue(context, MemberNames.NamespaceIndex.toString());
	}

	/**
	 * Set value of NamespaceIndex
	 * @param namespaceindex
	 */
	public final void setNamespaceIndex(java.lang.Integer namespaceindex)
	{
		setNamespaceIndex(getContext(), namespaceindex);
	}

	/**
	 * Set value of NamespaceIndex
	 * @param context
	 * @param namespaceindex
	 */
	public final void setNamespaceIndex(com.mendix.systemwideinterfaces.core.IContext context, java.lang.Integer namespaceindex)
	{
		getMendixObject().setValue(context, MemberNames.NamespaceIndex.toString(), namespaceindex);
	}

	/**
	 * @return value of Identifier
	 */
	public final java.lang.String getIdentifier()
	{
		return getIdentifier(getContext());
	}

	/**
	 * @param context
	 * @return value of Identifier
	 */
	public final java.lang.String getIdentifier(com.mendix.systemwideinterfaces.core.IContext context)
	{
		return (java.lang.String) getMendixObject().getValue(context, MemberNames.Identifier.toString());
	}

	/**
	 * Set value of Identifier
	 * @param identifier
	 */
	public final void setIdentifier(java.lang.String identifier)
	{
		setIdentifier(getContext(), identifier);
	}

	/**
	 * Set value of Identifier
	 * @param context
	 * @param identifier
	 */
	public final void setIdentifier(com.mendix.systemwideinterfaces.core.IContext context, java.lang.String identifier)
	{
		getMendixObject().setValue(context, MemberNames.Identifier.toString(), identifier);
	}

	/**
	 * @return value of _type
	 */
	public final java.lang.String get_type()
	{
		return get_type(getContext());
	}

	/**
	 * @param context
	 * @return value of _type
	 */
	public final java.lang.String get_type(com.mendix.systemwideinterfaces.core.IContext context)
	{
		return (java.lang.String) getMendixObject().getValue(context, MemberNames._type.toString());
	}

	/**
	 * Set value of _type
	 * @param _type
	 */
	public final void set_type(java.lang.String _type)
	{
		set_type(getContext(), _type);
	}

	/**
	 * Set value of _type
	 * @param context
	 * @param _type
	 */
	public final void set_type(com.mendix.systemwideinterfaces.core.IContext context, java.lang.String _type)
	{
		getMendixObject().setValue(context, MemberNames._type.toString(), _type);
	}

	/**
	 * @return value of _null
	 */
	public final java.lang.Boolean get_null()
	{
		return get_null(getContext());
	}

	/**
	 * @param context
	 * @return value of _null
	 */
	public final java.lang.Boolean get_null(com.mendix.systemwideinterfaces.core.IContext context)
	{
		return (java.lang.Boolean) getMendixObject().getValue(context, MemberNames._null.toString());
	}

	/**
	 * Set value of _null
	 * @param _null
	 */
	public final void set_null(java.lang.Boolean _null)
	{
		set_null(getContext(), _null);
	}

	/**
	 * Set value of _null
	 * @param context
	 * @param _null
	 */
	public final void set_null(com.mendix.systemwideinterfaces.core.IContext context, java.lang.Boolean _null)
	{
		getMendixObject().setValue(context, MemberNames._null.toString(), _null);
	}

	/**
	 * @return value of NotNull
	 */
	public final java.lang.Boolean getNotNull()
	{
		return getNotNull(getContext());
	}

	/**
	 * @param context
	 * @return value of NotNull
	 */
	public final java.lang.Boolean getNotNull(com.mendix.systemwideinterfaces.core.IContext context)
	{
		return (java.lang.Boolean) getMendixObject().getValue(context, MemberNames.NotNull.toString());
	}

	/**
	 * Set value of NotNull
	 * @param notnull
	 */
	public final void setNotNull(java.lang.Boolean notnull)
	{
		setNotNull(getContext(), notnull);
	}

	/**
	 * Set value of NotNull
	 * @param context
	 * @param notnull
	 */
	public final void setNotNull(com.mendix.systemwideinterfaces.core.IContext context, java.lang.Boolean notnull)
	{
		getMendixObject().setValue(context, MemberNames.NotNull.toString(), notnull);
	}

	/**
	 * @return value of ReferenceTypeId_OpcUaNode
	 */
	public final opcuaclient_exampleimplementation.proxies.OpcUaNode getReferenceTypeId_OpcUaNode() throws com.mendix.core.CoreException
	{
		return getReferenceTypeId_OpcUaNode(getContext());
	}

	/**
	 * @param context
	 * @return value of ReferenceTypeId_OpcUaNode
	 */
	public final opcuaclient_exampleimplementation.proxies.OpcUaNode getReferenceTypeId_OpcUaNode(com.mendix.systemwideinterfaces.core.IContext context) throws com.mendix.core.CoreException
	{
		opcuaclient_exampleimplementation.proxies.OpcUaNode result = null;
		com.mendix.systemwideinterfaces.core.IMendixIdentifier identifier = getMendixObject().getValue(context, MemberNames.ReferenceTypeId_OpcUaNode.toString());
		if (identifier != null)
			result = opcuaclient_exampleimplementation.proxies.OpcUaNode.load(context, identifier);
		return result;
	}

	/**
	 * Set value of ReferenceTypeId_OpcUaNode
	 * @param referencetypeid_opcuanode
	 */
	public final void setReferenceTypeId_OpcUaNode(opcuaclient_exampleimplementation.proxies.OpcUaNode referencetypeid_opcuanode)
	{
		setReferenceTypeId_OpcUaNode(getContext(), referencetypeid_opcuanode);
	}

	/**
	 * Set value of ReferenceTypeId_OpcUaNode
	 * @param context
	 * @param referencetypeid_opcuanode
	 */
	public final void setReferenceTypeId_OpcUaNode(com.mendix.systemwideinterfaces.core.IContext context, opcuaclient_exampleimplementation.proxies.OpcUaNode referencetypeid_opcuanode)
	{
		if (referencetypeid_opcuanode == null)
			getMendixObject().setValue(context, MemberNames.ReferenceTypeId_OpcUaNode.toString(), null);
		else
			getMendixObject().setValue(context, MemberNames.ReferenceTypeId_OpcUaNode.toString(), referencetypeid_opcuanode.getMendixObject().getId());
	}

	/**
	 * @return the IMendixObject instance of this proxy for use in the Core interface.
	 */
	public final com.mendix.systemwideinterfaces.core.IMendixObject getMendixObject()
	{
		return referenceTypeIdMendixObject;
	}

	/**
	 * @return the IContext instance of this proxy, or null if no IContext instance was specified at initialization.
	 */
	public final com.mendix.systemwideinterfaces.core.IContext getContext()
	{
		return context;
	}

	@java.lang.Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;

		if (obj != null && getClass().equals(obj.getClass()))
		{
			final opcuaclient_exampleimplementation.proxies.ReferenceTypeId that = (opcuaclient_exampleimplementation.proxies.ReferenceTypeId) obj;
			return getMendixObject().equals(that.getMendixObject());
		}
		return false;
	}

	@java.lang.Override
	public int hashCode()
	{
		return getMendixObject().hashCode();
	}

	/**
	 * @return String name of this class
	 */
	public static java.lang.String getType()
	{
		return "OpcUaClient_ExampleImplementation.ReferenceTypeId";
	}

	/**
	 * @return String GUID from this object, format: ID_0000000000
	 * @deprecated Use getMendixObject().getId().toLong() to get a unique identifier for this object.
	 */
	@java.lang.Deprecated
	public java.lang.String getGUID()
	{
		return "ID_" + getMendixObject().getId().toLong();
	}
}
