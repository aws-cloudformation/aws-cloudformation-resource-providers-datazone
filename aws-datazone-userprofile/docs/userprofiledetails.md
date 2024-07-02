# AWS::DataZone::UserProfile UserProfileDetails

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#iam" title="Iam">Iam</a>" : <i><a href="iamuserprofiledetails.md">IamUserProfileDetails</a></i>,
    "<a href="#sso" title="Sso">Sso</a>" : <i><a href="ssouserprofiledetails.md">SsoUserProfileDetails</a></i>
}
</pre>

### YAML

<pre>
<a href="#iam" title="Iam">Iam</a>: <i><a href="iamuserprofiledetails.md">IamUserProfileDetails</a></i>
<a href="#sso" title="Sso">Sso</a>: <i><a href="ssouserprofiledetails.md">SsoUserProfileDetails</a></i>
</pre>

## Properties

#### Iam

The details of the IAM User Profile.

_Required_: Yes

_Type_: <a href="iamuserprofiledetails.md">IamUserProfileDetails</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Sso

The details of the SSO User Profile.

_Required_: Yes

_Type_: <a href="ssouserprofiledetails.md">SsoUserProfileDetails</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

