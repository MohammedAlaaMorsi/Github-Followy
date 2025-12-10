package io.mohammedalaamorsi.followy.shared.data.models

/**
 * Represents the follow relationship status between users
 */
sealed class FollowRelationship {
    /**
     * Users who follow you but you don't follow back
     */
    data class FollowersNotFollowedBack(val users: List<GitHubUser>) : FollowRelationship()
    
    /**
     * Users you follow but they don't follow back
     */
    data class FollowingNotFollowingBack(val users: List<GitHubUser>) : FollowRelationship()
    
    /**
     * Users who mutually follow each other
     */
    data class MutualFollowers(val users: List<GitHubUser>) : FollowRelationship()
}
