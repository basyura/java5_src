/*
 * @(#)CertPathValidator.java	1.9 04/06/28
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.security.cert;

import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Security;
import sun.security.util.Debug;

import sun.security.jca.*;
import sun.security.jca.GetInstance.Instance;

/**
 * A class for validating certification paths (also known as certificate 
 * chains).
 * <p>
 * This class uses a provider-based architecture, as described in the Java 
 * Cryptography Architecture. To create a <code>CertPathValidator</code>, 
 * call one of the static <code>getInstance</code> methods, passing in the 
 * algorithm name of the <code>CertPathValidator</code> desired and 
 * optionally the name of the provider desired. 
 * <p>
 * Once a <code>CertPathValidator</code> object has been created, it can
 * be used to validate certification paths by calling the {@link #validate
 * validate} method and passing it the <code>CertPath</code> to be validated
 * and an algorithm-specific set of parameters. If successful, the result is
 * returned in an object that implements the 
 * <code>CertPathValidatorResult</code> interface.
 * <p>
 * <b>Concurrent Access</b>
 * <p>
 * The static methods of this class are guaranteed to be thread-safe.
 * Multiple threads may concurrently invoke the static methods defined in
 * this class with no ill effects.
 * <p>
 * However, this is not true for the non-static methods defined by this class.
 * Unless otherwise documented by a specific provider, threads that need to
 * access a single <code>CertPathValidator</code> instance concurrently should
 * synchronize amongst themselves and provide the necessary locking. Multiple
 * threads each manipulating a different <code>CertPathValidator</code>
 * instance need not synchronize.
 *
 * @see CertPath
 *
 * @version 	1.9 06/28/04
 * @since	1.4
 * @author	Yassir Elley
 */
public class CertPathValidator {

    /*
     * Constant to lookup in the Security properties file to determine
     * the default certpathvalidator type. In the Security properties file, 
     * the default certpathvalidator type is given as:
     * <pre>
     * certpathvalidator.type=PKIX
     * </pre>
     */
    private static final String CPV_TYPE = "certpathvalidator.type";
    private static final Debug debug = Debug.getInstance("certpath");
    private CertPathValidatorSpi validatorSpi;
    private Provider provider;
    private String algorithm;

    /**
     * Creates a <code>CertPathValidator</code> object of the given algorithm, 
     * and encapsulates the given provider implementation (SPI object) in it.
     *
     * @param validatorSpi the provider implementation
     * @param provider the provider
     * @param algorithm the algorithm name
     */
    protected CertPathValidator(CertPathValidatorSpi validatorSpi, 
	Provider provider, String algorithm) 
    {
	this.validatorSpi = validatorSpi;
	this.provider = provider;
	this.algorithm = algorithm;
    }
    
    /**
     * Returns a <code>CertPathValidator</code> object that implements the 
     * specified algorithm.
     *
     * <p> If the default provider package provides an implementation of the
     * specified <code>CertPathValidator</code> algorithm, an instance of 
     * <code>CertPathValidator</code> containing that implementation is 
     * returned. If the requested algorithm is not available in the default 
     * package, other packages are searched.
     * 
     * @param algorithm the name of the requested <code>CertPathValidator</code>
     * algorithm
     * @return a <code>CertPathValidator</code> object that implements the
     * specified algorithm
     * @exception NoSuchAlgorithmException if the requested algorithm
     * is not available in the default provider package or any of the other
     * provider packages that were searched
     */
    public static CertPathValidator getInstance(String algorithm)
	    throws NoSuchAlgorithmException {
	Instance instance = GetInstance.getInstance("CertPathValidator", 
	    CertPathValidatorSpi.class, algorithm);
	return new CertPathValidator((CertPathValidatorSpi)instance.impl,
	    instance.provider, algorithm);
    }

    /**
     * Returns a <code>CertPathValidator</code> object that implements the
     * specified algorithm, as supplied by the specified provider.
     *
     * @param algorithm the name of the requested <code>CertPathValidator</code>
     * algorithm
     * @param provider the name of the provider
     * @return a <code>CertPathValidator</code> object that implements the
     * specified algorithm, as supplied by the specified provider
     * @exception NoSuchAlgorithmException if the requested algorithm
     * is not available from the specified provider
     * @exception NoSuchProviderException if the provider has not been
     * configured
     * @exception IllegalArgumentException if the <code>provider</code> is
     * null
     */
    public static CertPathValidator getInstance(String algorithm, 
	    String provider) throws NoSuchAlgorithmException, 
	    NoSuchProviderException {
	Instance instance = GetInstance.getInstance("CertPathValidator", 
	    CertPathValidatorSpi.class, algorithm, provider);
	return new CertPathValidator((CertPathValidatorSpi)instance.impl,
	    instance.provider, algorithm);
    }
	  
    /**
     * Returns a <code>CertPathValidator</code> object that implements the
     * specified algorithm, as supplied by the specified provider.
     * Note: the <code>provider</code> doesn't have to be registered.
     *
     * @param algorithm the name of the requested 
     * <code>CertPathValidator</code> algorithm
     * @param provider the provider
     * @return a <code>CertPathValidator</code> object that implements the
     * specified algorithm, as supplied by the specified provider
     * @exception NoSuchAlgorithmException if the requested algorithm
     * is not available from the specified provider
     * @exception IllegalArgumentException if the <code>provider</code> is
     * null
     */
    public static CertPathValidator getInstance(String algorithm, 
	    Provider provider) throws NoSuchAlgorithmException {
	Instance instance = GetInstance.getInstance("CertPathValidator", 
	    CertPathValidatorSpi.class, algorithm, provider);
	return new CertPathValidator((CertPathValidatorSpi)instance.impl,
	    instance.provider, algorithm);
    }

    /**
     * Returns the <code>Provider</code> of this
     * <code>CertPathValidator</code>.
     *
     * @return the <code>Provider</code> of this <code>CertPathValidator</code>
     */
    public final Provider getProvider() {
	return this.provider;
    }

    /**
     * Returns the algorithm name of this <code>CertPathValidator</code>.
     *
     * @return the algorithm name of this <code>CertPathValidator</code>
     */
    public final String getAlgorithm() {
	return this.algorithm;
    }

    /**
     * Validates the specified certification path using the specified 
     * algorithm parameter set. 
     * <p>
     * The <code>CertPath</code> specified must be of a type that is 
     * supported by the validation algorithm, otherwise an
     * <code>InvalidAlgorithmParameterException</code> will be thrown. For 
     * example, a <code>CertPathValidator</code> that implements the PKIX
     * algorithm validates <code>CertPath</code> objects of type X.509.
     *
     * @param certPath the <code>CertPath</code> to be validated
     * @param params the algorithm parameters
     * @return the result of the validation algorithm
     * @exception CertPathValidatorException if the <code>CertPath</code>
     * does not validate
     * @exception InvalidAlgorithmParameterException if the specified 
     * parameters or the type of the specified <code>CertPath</code> are 
     * inappropriate for this <code>CertPathValidator</code>
     */ 
    public final CertPathValidatorResult validate(CertPath certPath, 
	CertPathParameters params)
	throws CertPathValidatorException, InvalidAlgorithmParameterException 
    {
	return validatorSpi.engineValidate(certPath, params);
    }

    /**
     * Returns the default <code>CertPathValidator</code> type as specified in 
     * the Java security properties file, or the string &quot;PKIX&quot;
     * if no such property exists. The Java security properties file is 
     * located in the file named &lt;JAVA_HOME&gt;/lib/security/java.security, 
     * where &lt;JAVA_HOME&gt; refers to the directory where the JDK was 
     * installed.
     *
     * <p>The default <code>CertPathValidator</code> type can be used by 
     * applications that do not want to use a hard-coded type when calling one 
     * of the <code>getInstance</code> methods, and want to provide a default 
     * type in case a user does not specify its own.
     *
     * <p>The default <code>CertPathValidator</code> type can be changed by 
     * setting the value of the "certpathvalidator.type" security property 
     * (in the Java security properties file) to the desired type.
     *
     * @return the default <code>CertPathValidator</code> type as specified 
     * in the Java security properties file, or the string &quot;PKIX&quot;
     * if no such property exists.
     */
    public final static String getDefaultType() {
        String cpvtype;
        cpvtype = (String)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return Security.getProperty(CPV_TYPE);
            }
        });
        if (cpvtype == null) {
            cpvtype = "PKIX";
        }
        return cpvtype;
    }
}
