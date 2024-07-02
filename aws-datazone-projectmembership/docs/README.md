# AWS::DataZone::ProjectMembership

Definition of AWS::DataZone::ProjectMembership Resource Type

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::DataZone::ProjectMembership",
    "Properties" : {
        "<a href="#designation" title="Designation">Designation</a>" : <i>String</i>,
        "<a href="#domainidentifier" title="DomainIdentifier">DomainIdentifier</a>" : <i>String</i>,
        "<a href="#member" title="Member">Member</a>" : <i><a href="member.md">Member</a></i>,
        "<a href="#projectidentifier" title="ProjectIdentifier">ProjectIdentifier</a>" : <i>String</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::DataZone::ProjectMembership
Properties:
    <a href="#designation" title="Designation">Designation</a>: <i>String</i>
    <a href="#domainidentifier" title="DomainIdentifier">DomainIdentifier</a>: <i>String</i>
    <a href="#member" title="Member">Member</a>: <i><a href="member.md">Member</a></i>
    <a href="#projectidentifier" title="ProjectIdentifier">ProjectIdentifier</a>: <i>String</i>
</pre>

## Properties

#### Designation

_Required_: Yes

_Type_: String

_Allowed Values_: <code>PROJECT_OWNER</code> | <code>PROJECT_CONTRIBUTOR</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DomainIdentifier

_Required_: Yes

_Type_: String

_Pattern_: <code>^dzd[-_][a-zA-Z0-9_-]{1,36}$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Member

_Required_: Yes

_Type_: <a href="member.md">Member</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ProjectIdentifier

_Required_: Yes

_Type_: String

_Pattern_: <code>^[a-zA-Z0-9_-]{1,36}$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

## Return Values

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### MemberIdentifier

Returns the <code>MemberIdentifier</code> value.

#### MemberIdentifierType

Returns the <code>MemberIdentifierType</code> value.

