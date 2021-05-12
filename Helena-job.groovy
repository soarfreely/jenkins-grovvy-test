
// 镜像仓库
def harborUri = 'harbor.vchangyi.com'

// 项目名称
def projectName='helena'

def helenaProject=[
        'test':'git@github.com:soarfreely/jenkins-grovvy-test.git',
]

// 配置的文件名
def confFileName = ['gw-console', 'gw-shop', 'micro-activity', ' micro-activity.conf']

// 默认发布分支
def defaultBranch='dev'

//部署
node {
    properties([

            buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '5')),

            // 参数
            parameters([
                    // 运行环境
                    string(name: 'runEnv', defaultValue: 'test', description: '''运行环境（发布时请手动修改环境标识，标识如下：）\n测试环境：test\n生产环境：prod\n'''),
                    // 分支
                    string(name: 'branch', defaultValue: 'dev', description: '''代码分支（发布时请手动修改分支标识，标识如下：）\n测试分支：test\n生产分支：master\n'''),
                    // 模块
                    choice(choices: ['gw-console', 'gw-work', 'demo'], description: '''手动修改发布的模块，标识如下：）
                console：gw-console
                work：gw-work
            ''', name: 'module')
            ])
    ])

    // 拉取业务代码
    for (repository in helenaProject) {
        stage('scm') {
            checkout([
                    $class                           : 'GitSCM',
                    branches                         : [[name: "${branch}"]],
                    doGenerateSubmoduleConfigurations: false,
                    extensions: [
                            [$class: 'SparseCheckoutPaths',  sparseCheckoutPaths:[[$class:'SparseCheckoutPath', path:'./ci/job/runtime/pack-dict']]]
                    ],
                    submoduleCfg                     : [],
                    userRemoteConfigs                : [[
                                                                credentialsId: "${pullCodeCredentialsId}",
                                                                url          : repository.value
                                                        ]]
            ])
        }
    }

    // 拉取配置文件
    stage('scm') {
        checkout([
                $class                           : 'GitSCM',
                branches                         : [[name: "dev"]], //TODO
                doGenerateSubmoduleConfigurations: false,
                extensions                       : [],
                submoduleCfg                     : [],
                userRemoteConfigs                : [[
                                                            credentialsId: "${pullCodeCredentialsId}",
                                                            url          : repository.value
                                                    ]]
        ])
    }

    // 构建镜像
    String helenaImageVersion = "helena-${module}-${runEnv}:${versionNo}"
    stage('build image') {
        sh """
                cp src/.env.${runEnv} src/.env
                docker build --no-cache -f ci/Dockerfile-job -t ${harborUri}/${harborNamespace}/${helenaImageVersion} .
        """
    }

    // 推送镜像
    stage('push image') {
        println("push image")
    }

}


// 配置文件内容拼接
def jointConfig() {
    String currentDir = new File(".").getAbsolutePath()
    String supervisordConfig = currentDir + '/ci/job/runtime/supervisord.confsupervisord.conf'

    File dir = new File(currentDir)

    for (file in dir.listFiles()) {
        if (file.isFile()) {
            Integer index = file.getName().lastIndexOf('.')
            String suffix = file.getName()[index..-1]

            if ('.config' == suffix) {
//                println(file.getName())
//                fileList.add(file.getName())
//                println(file.getAbsolutePath())
                String contentLine = readFile(file.getAbsolutePath())
                fileAppend(supervisordConfig, contentLine)
            }
        }
    }
}

// 文件追加
static def fileAppend(filePath, content) {
    File file = new File(filePath)

//    file.write("This is line1")
//    file << "\nThis is line2"
    file.append("\n${content}")
}


// 读文件
static def readFile (String filePath) {
    File file = new File(filePath)
    file.eachLine { line ->
        return line
    }
}

jointConfig()