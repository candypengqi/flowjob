/*
 *
 *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.limbo.flowjob.broker.core.cluster;

import java.util.Collection;

/**
 *
 * @author Devil
 * @since 2022/7/20
 */
public interface NodeManger {

    /**
     * 节点上线
     */
    void online(Node node);

    /**
     * 节点下线
     */
    void offline(Node node);

    /**
     * 检查节点是否存活
     */
    boolean alive(String name);

    /**
     * 所有存活节点
     */
    Collection<Node> allAlive();

}
