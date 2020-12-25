package com.weimob.webfwk.util.vcs.gitlab;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class GitlabSharedWithGroup {

	private Long groupId;
	private String groupName;
	private Long groupAccessLevel;
	private String expiresAt;

	public static GitlabGroup GitlabSharedWithGroupToGitlabGroup(
			GitlabSharedWithGroup swg) {
		return new GitlabGroup().setId(swg.getGroupId()).setName(
				swg.getGroupName());
	}
	
	public static List<GitlabGroup> GitlabSharedWithGroupsToGitlabGroups(
			List<GitlabSharedWithGroup> swg) {
		List<GitlabGroup> list = new ArrayList<GitlabGroup>();
		for(GitlabSharedWithGroup vo:swg){
			list.add(GitlabSharedWithGroupToGitlabGroup(vo));
		}
		return list;
	}
}
