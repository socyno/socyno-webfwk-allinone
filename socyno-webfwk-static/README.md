## 项目环境
基于vue-cli脚手架构建
```
包管理： yarn
代码管理： git
脚手架: vue-cli
三方库：vue, vuex, vue-Router, element-ui, webpack
```

## 代码规范
基于vuejs官方风格指南
1. 遵守eslint文件的规范
2. class属性以中线分隔 `-`
3. 变量名为驼峰，在HTML中则以中线分隔

## 代码commit规范
基于angular的contributing规范
```
<type>(<scope>): <subject>
`feat(login): add login page`
```  

### Type
Must be one of the following:

* **build**: Changes that affect the build system or external dependencies (example scopes: gulp, broccoli, npm)
* **ci**: Changes to our CI configuration files and scripts (example scopes: Circle, BrowserStack, SauceLabs)
* **docs**: Documentation only changes
* **feat**: A new feature
* **fix**: A bug fix
* **perf**: A code change that improves performance
* **refactor**: A code change that neither fixes a bug or adds a feature
* **style**: Changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc)
* **test**: Adding missing tests or correcting existing tests
* **chore**: some useless modification that do not affect other functions

### Scope
The scope should be the name of the npm package affected (as perceived by the person reading the changelog generated from commit messages.

### Subject
The subject contains a succinct description of the change:

- use the imperative, present tense: "change" not "changed" nor "changes"
- don't capitalize the first letter
- no dot (.) at the end
## 单元测试规范
  使用jest框架,主要目的覆盖功能性的js文件 
  在tests/unit文件夹中新建xxx.spec.js测试文件
