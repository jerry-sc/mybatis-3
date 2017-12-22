/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
/**
 * 此模块主要用于创建映射文件与mapper接口之间的对应关系，防止用户出现拼写错误，导致错误直到运行期间才能发现。通过创建两者映射，使得错误最早能在启动时发现，因为启动时会验证
 * 映射文件中SQL的id与映射文件的方法名称是否一致，如果不一致，会导致启动失败
 * Bings mapper interfaces with mapped statements
 */
package org.apache.ibatis.binding;
