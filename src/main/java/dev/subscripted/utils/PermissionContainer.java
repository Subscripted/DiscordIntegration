package dev.subscripted.utils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PermissionContainer {


    final SmartConfig c;


    /**
     * @param member        Checked User for Permission
     * @param permissionKey Permissionkey
     * @return true, when user has specified permission
     */
    public boolean hasPermission(Member member, String permissionKey) {
        String pathToAllUser = "permissions.all.user";
        String pathToAllRole = "permissions.all.role";

        List<String> allUserIds = (List<String>) c.getList(pathToAllUser);
        if (allUserIds != null && allUserIds.contains(member.getId())) {
            return true;
        }

        List<String> allRoleIds = (List<String>) c.getList(pathToAllRole);
        if (allRoleIds != null) {
            for (Role role : member.getRoles()) {
                if (allRoleIds.contains(role.getId())) {
                    return true;
                }
            }
        }
        String pathToUser = "permissions." + permissionKey + ".user";
        String pathToRole = "permissions." + permissionKey + ".role";

        List<String> userIds = (List<String>) c.getList(pathToUser);
        if (userIds != null && userIds.contains(member.getId())) {
            return true;
        }

        List<String> roleIds = (List<String>) c.getList(pathToRole);
        if (roleIds != null) {
            for (Role role : member.getRoles()) {
                if (roleIds.contains(role.getId())) {
                    return true;
                }
            }
        }

        return false;
    }


    public void addUserPermission(String permissionKey, String userId) {
        String userPath = "permissions." + permissionKey + ".user";
        List<String> userIds = (List<String>) c.getList(userPath);

        if (userIds == null || !userIds.contains(userId)) {
            if (userIds == null) {
                c.addList(userPath, List.of(userId));
            } else {
                userIds.add(userId);
                c.overwrite(userPath, userIds);
            }
        }
    }

    public void addRolePermission(String permissionKey, String roleId) {
        String rolePath = "permissions." + permissionKey + ".role";
        List<String> roleIds = (List<String>) c.getList(rolePath);

        if (roleIds == null || !roleIds.contains(roleId)) {
            if (roleIds == null) {
                c.addList(rolePath, List.of(roleId));
            } else {
                roleIds.add(roleId);
                c.overwrite(rolePath, roleIds);
            }
        }
    }

    public void removeUserPermission(String permissionKey, String userId) {
        String userPath = "permissions." + permissionKey + ".user";
        List<String> userIds = (List<String>) c.getList(userPath);

        if (userIds != null && userIds.contains(userId)) {
            userIds.remove(userId);
            c.overwrite(userPath, userIds);
            c.save();
        }
    }

    public void removeRolePermission(String permissionKey, String roleId) {
        String rolePath = "permissions." + permissionKey + ".role";
        List<String> roleIds = (List<String>) c.getList(rolePath);

        if (roleIds != null && roleIds.contains(roleId)) {
            roleIds.remove(roleId);
            c.overwrite(rolePath, roleIds);
            c.save();
        }
    }
}
