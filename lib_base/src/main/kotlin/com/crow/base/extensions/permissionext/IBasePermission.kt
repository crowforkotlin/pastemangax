package com.crow.base.extensions.permissionext

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/com/barry/base/extensions/permissionext
 * @Time: 2022/11/16 10:46
 * @Author: BarryAllen
 * @Description: IBasePermission
 * @formatter:on
 **************************/
interface IBasePermission {

    var iBasePerEvent: IBasePerEvent?

    fun requestPermission(permissions: Array<String>, iBasePerEvent: IBasePerEvent)

}