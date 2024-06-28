# AWS::DataZone::UserProfile

A user profile represents Amazon DataZone users. Amazon DataZone supports both IAM roles and SSO identities to interact with the Amazon DataZone Management Console and the data portal for different purposes. Domain administrators use IAM roles to perform the initial administrative domain-related work in the Amazon DataZone Management Console, including creating new Amazon DataZone domains, configuring metadata form types, and implementing policies. Data workers use their SSO corporate identities via Identity Center to log into the Amazon DataZone Data Portal and access projects where they have memberships.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::DataZone::UserProfile",
    "Properties" : {
        "<a href="#details" title="Details">Details</a>" : <i><a href="userprofiledetails.md">UserProfileDetails</a></i>,
        "<a href="#domainidentifier" title="DomainIdentifier">DomainIdentifier</a>" : <i>String</i>,
        "<a href="#status" title="Status">Status</a>" : <i>String</i>,
        "<a href="#useridentifier" title="UserIdentifier">UserIdentifier</a>" : <i>String</i>,
        "<a href="#usertype" title="UserType">UserType</a>" : <i>String</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::DataZone::UserProfile
Properties:
    <a href="#details" title="Details">Details</a>: <i><a href="userprofiledetails.md">UserProfileDetails</a></i>
    <a href="#domainidentifier" title="DomainIdentifier">DomainIdentifier</a>: <i>String</i>
    <a href="#status" title="Status">Status</a>: <i>String</i>
    <a href="#useridentifier" title="UserIdentifier">UserIdentifier</a>: <i>String</i>
    <a href="#usertype" title="UserType">UserType</a>: <i>String</i>
</pre>

## Properties

#### Details

_Required_: No

_Type_: <a href="userprofiledetails.md">UserProfileDetails</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DomainIdentifier

The identifier of the Amazon DataZone domain in which the user profile would be created.

_Required_: Yes

_Type_: String

_Pattern_: <code>^dzd[-_][a-zA-Z0-9_-]{1,36}$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Status

The status of the user profile.

_Required_: No

_Type_: String

_Allowed Values_: <code>ASSIGNED</code> | <code>NOT_ASSIGNED</code> | <code>ACTIVATED</code> | <code>DEACTIVATED</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### UserIdentifier

The ID of the user.

_Required_: Yes

_Type_: String

_Pattern_: <code>(^([0-9a-f]{10}-|)[A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12}$|^[a-zA-Z_0-9+=,.@-]+$|^arn:aws:iam::\d{12}:.+$)</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### UserType

The type of the user.

_Required_: No

_Type_: String

_Allowed Values_: <code>IAM_USER</code> | <code>IAM_ROLE</code> | <code>SSO_USER</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

## Return Values

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### DomainId

The identifier of the Amazon DataZone domain in which the user profile is created.

#### Type

The type of the user profile.

#### Id

The ID of the Amazon DataZone user profile.

#### Details

Returns the <code>Details</code> value.

