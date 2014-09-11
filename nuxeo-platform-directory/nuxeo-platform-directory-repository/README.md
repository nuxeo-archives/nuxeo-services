nuxeo-platform-directory-repository
=========================

## What is this project ?

Nuxeo Directory Repository is a simple Addon for Nuxeo Platforms that allows to set-up a resilient directory ahead of standard sub directories such as LDAP or SQL directories

The aim is to provide a fallback behavior when the master subdirectory falls.

## Why would you use this ?

You should consider this as a plugin that you can use as a patch when you have some bad LDAP or bad network

Typical use case ensure that users can login even if the master subdirectory (ex:LDAP) is not available. You should add more slaves subdirectory to ensure the application availability.

(useful sample configuration can be found in the src/test/resources folder)
The configuration require the following steps :
 1-Define your LDAP/SQL directories : 
       - Define 2 standard LDAP directories (One for users the other one for groups) and add references
       - Define 2 standard SQL directories for mirroring (One for users, one for groups and add references)
       Example :<br/>
       - LDAP directory config :
       
       <directory name="ldapUserDirectory">
       ...
       add reference to ldap group directory
       </directory>
       <directory name="ldapGroupDirectory">
       ...
       add reference to ldap user directory
       </directory>
       
  	   - SQL directory config :
              
       <directory name="sqlUserDirectory">
       ...
       add reference to sql group directory
       </directory>
       <directory name="sqlGroupDirectory">
       ...
       add reference to sql user directory
       </directory>
       
 2-Define your resilient directory :
        - Define one resilient directory for users
        - Add as sub directories your LDAP and SQL users directories
        - Flag the LDAP as "master"
        - Define one resilient directory for groups
        - Add as sub directories your LDAP and SQL groups directories
        - Flag the LDAP as "master"
 
 Example :
 
        <directory name ="resilientUserDirectory">
        
            <subDirectory name="ldapUserDirectory" master="true"/>

            <subDirectory name="sqlUserDirectory"/>
            
        </directory>
        
        <directory name="resilientGroupDirectory">
        
            <subDirectory name="ldapGroupDirectory" master="true"/>

            <subDirectory name="sqlGroupDirectory"/>
            
        </directory>
 
3-Add a contrib for userManager service and extend the standard userManager :
        - Set as "users" your resilient user directory
        - Set as "groups" your resilient group directory
Example :

      <userManager>
	      <defaultAdministratorId>Administrator</defaultAdministratorId>
	      <administratorsGroup>administrators</administratorsGroup>
	      <defaultGroup>members</defaultGroup>
	      <users>
	        <directory>resilientUserDirectory</directory>
	      </users>
	      <groups>
	        <directory>resilientGroupDirectory</directory>
	      </groups>
    </userManager>       

## Prerequisite 
Prerequisite to add a resilient directory:
<ul>
<li>Can have only ONE master</li>
<li>At least one slave in write mode</li>
<li>The slave's schema must be the same than the master</li>
<li>The slave must used the same idField/passworField than the master</li>
<li>All SQL sub directory must not use auto-increment id</li>
<li>All master definitions (schema,mode,fields) will be used as the resilient definitions.</li>
<li>No need to duplicate definition in the resilient config  </li>
</ul>

## History

This code was initially written against a Nuxeo 5.9.6 


