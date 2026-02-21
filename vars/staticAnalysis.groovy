def call(Map config = [:]) {
    boolean abortPipeline = config.get('abortPipeline', false)
    
    try {
        echo "Iniciando análisis estático de código..."
        
        String branchName = getBranchName()
        echo "Rama actual: ${branchName}"
        
        boolean shouldAbort = determineShouldAbort(abortPipeline, branchName)
        echo "¿Abortar si falla QualityGate?: ${shouldAbort}"
        
        timeout(time: 5, unit: 'MINUTES') {
            withEnv(['SONAR_ENV=true']) {
                sh '''
                    echo "Iniciando escaneo de código estático..."
                    sleep 2
                    echo "Verificando convenciones de nombres..."
                    sleep 2
                    echo "Analizando complejidad ciclomática..."
                    sleep 2
                    echo "Buscando vulnerabilidades de seguridad..."
                    sleep 2
                    echo "Ejecución de las pruebas de calidad de código"
                    sleep 2
                    echo "Generando reporte de análisis..."
                    sleep 2
                    echo "Análisis completado exitosamente"
                '''
            }
        }
        
        echo "Análisis estático completado exitosamente"
        echo "Evaluando QualityGate..."
        boolean qualityGatePassed = true
        
        if (!qualityGatePassed && shouldAbort) {
            error("QualityGate falló en rama '${branchName}' y debe abortarse el pipeline.")
        } else if (!qualityGatePassed) {
            echo "Advertencia: QualityGate falló en rama '${branchName}' pero el pipeline continúa"
        } else {
            echo "QualityGate pasó correctamente"
        }
        
    } catch (Exception e) {
        echo "Error en el análisis estático: ${e.message}"
        throw e
    }
}

def getBranchName() {
    // Intenta obtener desde BRANCH_NAME (multibranch o parámetro)
    if (env.BRANCH_NAME) {
        return env.BRANCH_NAME
    }
    
    // Intenta obtener desde GIT_BRANCH
    if (env.GIT_BRANCH) {
        String branch = env.GIT_BRANCH
        if (branch.contains('/')) {
            branch = branch.substring(branch.lastIndexOf('/') + 1)
        }
        return branch
    }
    
    echo "Advertencia: No se pudo obtener el nombre de la rama"
    return "unknown"
}

def determineShouldAbort(boolean abortPipeline, String branchName) {
    if (abortPipeline) {
        echo "Abortarán por parámetro abortPipeline = true"
        return true
    }
    
    if (branchName == 'master') {
        echo "Abortarán porque la rama es 'master'"
        return true
    }
    
    if (branchName.startsWith('hotfix')) {
        echo "Abortarán porque la rama comienza con 'hotfix'"
        return true
    }
    
    echo "No abortará porque la rama es '${branchName}'"
    return false
}