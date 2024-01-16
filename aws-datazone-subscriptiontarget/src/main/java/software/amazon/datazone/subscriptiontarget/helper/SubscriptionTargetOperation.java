package software.amazon.datazone.subscriptiontarget.helper;

import lombok.Getter;

@Getter
public enum SubscriptionTargetOperation {
    CREATE_SUBSCRIPTION_TARGET("CreateSubscriptionTarget"),
    GET_SUBSCRIPTION_TARGET("GetSubscriptionTarget"),
    UPDATE_SUBSCRIPTION_TARGET("UpdateSubscriptionTarget"),
    DELETE_SUBSCRIPTION_TARGET("DeleteSubscriptionTarget"),
    LIST_SUBSCRIPTION_TARGET("ListSubscriptionTarget");

    private final String name;

    SubscriptionTargetOperation(final String name) {
        this.name = name;
    }

}
