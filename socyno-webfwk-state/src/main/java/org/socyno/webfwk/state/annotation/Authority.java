package org.socyno.webfwk.state.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.socyno.webfwk.state.authority.AuthorityScopeIdMultipleCleaner;
import org.socyno.webfwk.state.authority.AuthorityScopeIdMultipleParser;
import org.socyno.webfwk.state.authority.AuthorityScopeIdNoopMultipleCleaner;
import org.socyno.webfwk.state.authority.AuthorityScopeIdNoopMultipleParser;
import org.socyno.webfwk.state.authority.AuthorityScopeIdNoopParser;
import org.socyno.webfwk.state.authority.AuthorityScopeIdParser;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.authority.AuthoritySpecialChecker;
import org.socyno.webfwk.state.authority.AuthoritySpecialNoopChecker;
import org.socyno.webfwk.state.authority.AuthoritySpecialNoopRejecter;
import org.socyno.webfwk.state.authority.AuthoritySpecialRejecter;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Authority {
    /**
     * 授权范围：目前分为 Guest(匿名)，System(全局)和 Business(业务)三个范围。Guest 即无需任何授权，通常仅限系统
     * 登陆、或常量数据访问等操作； System 即授权只跟用户的角色相关，不存在具体授权标的对象，只要拥有角色即拥有被赋予的
     * 操作授权； Business，该授权范围相对特殊，授权同时与角色和系统业务绑定，即在授权时需针对不同的系统业务授予不同的
     * 角色，这样即可达到统一用户在不同系统业务具有不同的操作授权的目标，实现授权控制的精细化。
     */
    AuthorityScopeType value() ;
    
    /**
     * 获取授权标的所依赖的参数索引，只在接口上使用时有效，即如果制定的授权范围是与授权标的相关的（即 checkScopeId 返回
     * true)，那么就必须知道从哪个参数上可以获取授权标的信息，然后将该参数传递个授权标的解析器(parser)进行解析。针对后
     * 端流程引擎的使用场景，该参数是固定的，要么是当前的流程单（非新建），要么就是要创建的流程单（新建时），无需关注。
     */
    int paramIndex() default -1;
    
    /**
     * 授权标的解析器，只针对支持 checkScopeId 验证的 AuthorityScopeType 生效（即需要根据授权范围标的来决定权限场景）
     */
    Class<? extends AuthorityScopeIdParser> parser() default AuthorityScopeIdNoopParser.class;
    
    /**
     * 针对系统内建授权机制的授权放行处理器，使用场景为默认不授权，但此处校验返回 true 的允许执行。
     * 
     * 最典型的使用场景就是允许流程单的创建人执行修改或撤回的操作。通常如果通过系统内建的授权机制，授予某些角色可编辑的
     * 权限，那就意味着拥有这些角色的用户是可以修改任何人提交的流程单的，这种方式是无法实现只允许用户修改自己创建的流程
     * 单的目标，在这种场景下通常无需给申请人赋予可编辑权限的，而是通过 checker 授权机制检查当登陆用户与流程单创建用户
     * 相同即放行的方式来实现。
     *
     */
    Class<? extends AuthoritySpecialChecker> checker() default AuthoritySpecialNoopChecker.class;
    
    /**
     * 针对系统内建授权机制的授权拒绝处理器，其使用场景恰恰与 checker 相反，即已给予授权但在某些特定场景下必须禁止执行。
     * 
     * 可参考的使用场景：如流程单中的某个环节需要某个角色的用户人工修改配置，然后需要有同一角色内的另一成员进行复核。通常
     * 在系统内建授权机制上会授予该角色确认执行和确认复核的授权，如果此时不做任何控制，因成员同时会拥有这两项操作权限，那
     * 么原则上同一用户对同一流程单即可以确认执行也可以确认复核，那就很可能达不到复核的效果，这时就可以通过 rejecter 机制
     * 来校验确认复核的人与之确认执行的是否为同一用户，如果是则返回 true ，这样此人就无法复核由自己执行的流程单，已达到更
     * 好的流程效果。
     *
     */
    Class<? extends AuthoritySpecialRejecter> rejecter() default AuthoritySpecialNoopRejecter.class;
    
    /**
     * 为 parser 的一种补充机制，即某些场景下流程单可能涉及授权范围内的多个标的，这时允许返回多个授权标的信息
     */
    Class<? extends AuthorityScopeIdMultipleParser> multipleParser() default AuthorityScopeIdNoopMultipleParser.class;
    
    /**
     * 多授权标的场景下，是否开启操作选择器, 默认不开启，此时要求执行操作的用户拥有所有授权标的授权（通过 multipleOneAllowed
     * 开关可允许拥有任一授权标的用户执行操作）; 如果开启，则拥有任一授权标的的用户即可执行，但只有当多人协作使得涉及的多个授
     * 权标的均被覆盖到后，状态才会往下一节点流转，否则流程状态始终保持不变。     
     */
    boolean multipleChoice() default false;
    
    /**
     * 参见 multipleChoice， 即在不开启 multipleChoice 功能的情况下，允许拥有任一授权标的的用户执行流程操作，并直接流转
     * 至下一流程状态。
     */
    boolean multipleOneAllowed() default false;
    
    /**
     * 配合 multipleChoice 使用， 在 multipleChoice 场景下会记录哪些涉及的授权标的已经执行过事件，但当某些事件使得状态回退时
     * 是必须清除这些记录以便重新记录的，那么在这些会导致流程回退的事件上就需要添加这个 multipleCleaner 来告知清楚哪些事件产生
     * 的记录。
     */
    Class<? extends AuthorityScopeIdMultipleCleaner> multipleCleaner() default AuthorityScopeIdNoopMultipleCleaner.class;
}
