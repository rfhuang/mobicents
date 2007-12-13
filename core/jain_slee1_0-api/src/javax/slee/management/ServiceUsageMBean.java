package javax.slee.management;

import javax.slee.SbbID;
import javax.slee.ServiceID;
import javax.slee.InvalidArgumentException;
import javax.slee.UnrecognizedSbbException;
import javax.slee.usage.UnrecognizedUsageParameterSetNameException;
import javax.management.ObjectName;

/**
 * The <code>ServiceUsageMBean</code> interface defines service usage-related
 * management operations.  Using a <code>ServiceUsageMBean</code> object a management
 * client may get access to <code>SbbUsageMBean</code> objects, reset the usage
 * parameters for an individual SBB used in the represented Service, reset 
 * usage parameters for all SBBs used in the represented Service, or modify the
 * list of named usage parameter sets an SBB is allowed to use.
 * <p>
 * Each SBB maintains seperate sets of usage information for each Service that it
 * participates in.   A <code>ServiceUsageMBean</code> object provides management of
 * usage information accumulated for a single Service for all the SBBs participating
 * in that Service.
 * <p>
 * The Object Name of a <code>ServiceUsageMBean</code> object can be obtained by
 * a management client via the {@link ServiceManagementMBean#getServiceUsageMBean}
 * method.
 */
public interface ServiceUsageMBean {
    /**
     * Get the component identifier of the Service that this MBean provides usage
     * management access for.
     * @return the component identifier of the Service that this MBean provides usage
     *        management access for.
     * @throws ManagementException if the Service component identifier could not be
     *        obtained due to a system-level failure.
     */
    public ServiceID getService()
        throws ManagementException;

    /**
     * Create a new usage parameter set that the specified SBB is permitted to use in the
     * one-argument form of the get-usage-parameters methods, when the service component
     * identifer argument of the get-usage-parameters method identifies the Service that
     * this MBean provides usage management access for.
     * @param id the component identifier of the SBB.  The SBB must be an SBB that is
     *        used in the Service whose usage information is being managed by this MBean.
     * @param name the usage parameter set name.  Names must be non-null and greater than 0
     *        in length.
     * @throws NullPointerException if either argument is <code>null</code>.
     * @throws UnrecognizedSbbException if <code>id</code> is not a recognizable
     *        <code>SbbID</code> for the SLEE, does not correspond with an SBB installed
     *        in the SLEE, or is not an SBB that participates in the Service whose usage
     *        information is being managed by this MBean.
     * @throws InvalidArgumentException if <code>name</code> is zero-length or the identified
     *        SBB participates in this service but does not define a usage parameters interface.
     * @throws UsageParameterSetNameAlreadyExistsException if the name has already been used to
     *        create a usage parameter set for the SBB.
     * @throws ManagementException if the usage parameter set could not be created due to
     *        a system-level failure.
     */
    public void createUsageParameterSet(SbbID id, String name)
        throws NullPointerException, UnrecognizedSbbException,
               InvalidArgumentException, UsageParameterSetNameAlreadyExistsException,
               ManagementException;

    /**
     * Remove an existing usage parameter set that the specified SBB is permitted to
     * use in the one-argument form of the get-usage-parameters methods, when the service
     * component identifer argument of the get-usage-parameters method identifies the Service
     * that this MBean provides usage management access for.
     * @param id the component identifier of the SBB.  The SBB must be an SBB that is
     *        used in the Service whose usage information is being managed by this MBean.
     * @param name the usage parameter set name.
     * @throws NullPointerException if either argument is <code>null</code>.
     * @throws UnrecognizedSbbException if <code>id</code> is not a recognizable
     *        <code>SbbID</code> for the SLEE, does not correspond with an SBB installed
     *        in the SLEE, or is not an SBB that participates in the Service whose usage
     *        information is being managed by this MBean.
     * @throws InvalidArgumentException if the identified SBB participates in this service
     *        but does not define a usage parameters interface.
     * @throws UnrecognizedUsageParameterSetNameException if the name does not identify a
     *        usage parameter set that has been created for the SBB.
     * @throws ManagementException if the name could not be removed due to a system-level failure.
     */
    public void removeUsageParameterSet(SbbID id, String name)
        throws NullPointerException, UnrecognizedSbbException,
               InvalidArgumentException, UnrecognizedUsageParameterSetNameException,
               ManagementException;

    /**
     * Get the names of the usage parameter sets that the specified SBB is permitted to use
     * in the one-argument form of the get-usage-parameters methods, when the service component
     * identifer argument of the get-usage-parameters method identifies the Service that
     * this MBean provides usage management access for.
     * @param id the component identifier of the SBB.  The SBB must be an SBB that is
     *        used in the Service whose usage information is being managed by this MBean.
     * @return the names of the usage parameter sets that the SBB can use.
     * @throws NullPointerException if either argument is <code>null</code>.
     * @throws InvalidArgumentException if the identified SBB participates in this service
     *        but does not define a usage parameters interface.
     * @throws UnrecognizedSbbException if <code>id</code> is not a recognizable
     *        <code>SbbID</code> for the SLEE, does not correspond with an SBB installed
     *        in the SLEE, or is not an SBB that participates in the Service whose usage
     *        information is being managed by this MBean.
     * @throws ManagementException if the names could not be obtained due to a system-level
     *        failure.
     */
    public String[] getUsageParameterSets(SbbID id)
        throws NullPointerException, UnrecognizedSbbException, 
               InvalidArgumentException, ManagementException;

    /**
     * Get the JMX Object Name of an <code>SbbUsageMBean</code> object that provides
     * management access to the unamed usage parameter set for the specified SBB.  The SBB
     * must be participating in the Service that this MBean provides usage management access
     * for, and must have defined a usage parameter interface.
     * @param id the component identifier of the SBB.  The SBB must be an SBB that is
     *        used in the Service whose usage information is being managed by this MBean.
     * @return the Object Name of an <code>SbbUsageMBean</code> object.
     * @throws NullPointerException if <code>id</code> is <code>null</code>.
     * @throws UnrecognizedSbbException if <code>id</code> is not a recognizable
     *        <code>SbbID</code> for the SLEE, does not correspond with an SBB installed
     *        in the SLEE, or is not an SBB that participates in the Service whose usage
     *        information is being managed by this MBean.
     * @throws InvalidArgumentException if the identified SBB participates in this service
     *        but does not define a usage parameters interface.
     * @throws ManagementException if the object name could not be obtained due to a
     *        system-level failure.
     */
    public ObjectName getSbbUsageMBean(SbbID id)
        throws NullPointerException, UnrecognizedSbbException,
               InvalidArgumentException, ManagementException;

    /**
     * Get the JMX Object Name of an <code>SbbUsageMBean</code> object that provides
     * management access to the named usage parameter set for the specified SBB.  The SBB
     * must be participating in the Service that this MBean provides usage management access
     * for, and must have defined a usage parameter interface.
     * @param id the component identifier of the SBB.  The SBB must be an SBB that is
     *        used in the Service whose usage information is being managed by this MBean.
     * @param name the name of the usage parameter set.  The name must be one of the names
     *        returned by {@link #getUsageParameterSets getUsageParameterSets}<code>(id)</code>.
     * @return the Object Name of an <code>SbbUsageMBean</code> object.
     * @throws NullPointerException if either parameter is <code>null</code>.
     * @throws UnrecognizedSbbException if <code>id</code> is not a recognizable
     *        <code>SbbID</code> for the SLEE, does not correspond with an SBB installed
     *        in the SLEE, or is not an SBB that participates in the Service whose usage
     *        information is being managed by this MBean.
     * @throws InvalidArgumentException if the identified SBB participates in this service
     *        but does not define a usage parameters interface.
     * @throws UnrecognizedUsageParameterSetNameException if the named usage parameter set
     *        has not been created for the SBB.
     * @throws ManagementException if the object name could not be obtained due to a
     *        system-level failure.
     */
    public ObjectName getSbbUsageMBean(SbbID id, String name)
        throws NullPointerException, UnrecognizedSbbException,
               InvalidArgumentException, UnrecognizedUsageParameterSetNameException,
               ManagementException;

    /**
     * Reset all usage parameters in the unamed usage parameter set, and all named usage
     * parameter sets, of the specified SBB.  Counter-type usage parameters are reset to
     * <tt>0</tt> and sample-type usage parameters have all samples deleted.
     * @param id the component identifier of the SBB.  The SBB must be an SBB that is
     *        used in the Service whose usage information is being managed by this MBean.
     * @throws NullPointerException if <code>id</code> is <code>null</code>.
     * @throws UnrecognizedSbbException if <code>id</code> is not a recognizable
     *        <code>SbbID</code> for the SLEE, does not correspond with an SBB installed
     *        in the SLEE, or is not an SBB that participates in the Service whose usage
     *        information is being managed by this MBean.
     * @throws InvalidArgumentException if the identified SBB participates in this service
     *        but does not define a usage parameters interface.
     * @throws ManagementException if the values of the usage parameters could not be
     *        reset due to a system-level failure.
     */
    public void resetAllUsageParameters(SbbID id)
        throws NullPointerException, UnrecognizedSbbException, 
               InvalidArgumentException, ManagementException;
    /**
     * Reset all usage parameters in the unamed usage parameter set, and all named usage
     * parameter sets, of all SBB participating in the Service whose usage information is
     * being managed by this MBean.  Counter-type usage parameters are reset to <tt>0</tt>
     * and sample-type usage parameters have all samples cleared.
     * @throws ManagementException if the values of the usage parameters could not be
     *        reset due to a system-level failure.
     */
    public void resetAllUsageParameters()
        throws ManagementException;

    /**
     * Notify the SLEE that the service usage MBean is no longer required by the management
     * client.  As the SLEE may subsequently deregister the usage MBean from the MBean
     * server, a client that invokes this method should assume that the Object Name they
     * had for the MBean is no longer valid once this method returns.
     * @throws ManagementException if the service usage MBean could not be closed by the SLEE
     *       due to a system-level failure.
     */
    public void close()
        throws ManagementException;
}

