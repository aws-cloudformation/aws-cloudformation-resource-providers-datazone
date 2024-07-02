# AWS::DataZone::GroupProfile

Group profiles represent groups of Amazon DataZone users. Groups can be manually created, or mapped to Active Directory groups of enterprise customers. In Amazon DataZone, groups serve two purposes. First, a group can map to a team of users in the organizational chart, and thus reduce the administrative work of a Amazon DataZone project owner when there are new employees joining or leaving a team. Second, corporate administrators use Active Directory groups to manage and update user statuses and so Amazon DataZone domain administrators can use these group memberships to implement Amazon DataZone domain policies.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::DataZone::GroupProfile",
    "Properties" : {
        "<a href="#domainidentifier" title="DomainIdentifier">DomainIdentifier</a>" : <i>String</i>,
        "<a href="#groupidentifier" title="GroupIdentifier">GroupIdentifier</a>" : <i>String</i>,
        "<a href="#status" title="Status">Status</a>" : <i>String</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::DataZone::GroupProfile
Properties:
    <a href="#domainidentifier" title="DomainIdentifier">DomainIdentifier</a>: <i>String</i>
    <a href="#groupidentifier" title="GroupIdentifier">GroupIdentifier</a>: <i>String</i>
    <a href="#status" title="Status">Status</a>: <i>String</i>
</pre>

## Properties

#### DomainIdentifier

The identifier of the Amazon DataZone domain in which the group profile would be created.

_Required_: Yes

_Type_: String

_Pattern_: <code>^dzd[-_][a-zA-Z0-9_-]{1,36}$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### GroupIdentifier

The ID of the group.

_Required_: Yes

_Type_: String

_Pattern_: <code>(^([0-9a-f]{10}-|)[A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12}$|[\p{L}\p{M}\p{S}\p{N}\p{P}\t\n\r  ]+)</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Status

The status of the group profile.

_Required_: No

_Type_: String

_Allowed Values_: <code>ASSIGNED</code> | <code>NOT_ASSIGNED</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### DomainId

The identifier of the Amazon DataZone domain in which the group profile is created.

#### GroupName

The group-name of the Group Profile.

#### Id

The ID of the Amazon DataZone group profile.

