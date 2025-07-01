// Configuración global
const API_BASE_URL = 'http://localhost:8080/api';

// Estado global de la aplicación
let currentSection = 'dashboard';
let clientesData = [];
let estadisticasData = {};

// Inicialización cuando se carga la página
document.addEventListener('DOMContentLoaded', function() {
    inicializarApp();
});

// Función principal de inicialización
function inicializarApp() {
    console.log('🚀 Inicializando aplicación frontend...');
    
    // Configurar navegación
    configurarNavegacion();
    
    // Configurar formularios
    configurarFormularios();
    
    // Cargar datos iniciales
    cargarDatosIniciales();
    
    // Configurar actualizaciones automáticas
    setInterval(actualizarEstadisticasHeader, 30000); // Cada 30 segundos
    
    console.log('✅ Aplicación frontend inicializada');
}

// Configurar navegación entre secciones
function configurarNavegacion() {
    const navItems = document.querySelectorAll('.nav-item');
    
    navItems.forEach(item => {
        item.addEventListener('click', function() {
            const section = this.getAttribute('data-section');
            switchSection(section);
        });
    });
}

// Cambiar entre secciones
function switchSection(sectionName) {
    // Actualizar estado
    currentSection = sectionName;
    
    // Ocultar todas las secciones
    document.querySelectorAll('.content-section').forEach(section => {
        section.classList.remove('active');
    });
    
    // Mostrar la sección seleccionada
    const targetSection = document.getElementById(sectionName);
    if (targetSection) {
        targetSection.classList.add('active');
    }
    
    // Actualizar navegación activa
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
    });
    
    const activeNavItem = document.querySelector(`[data-section="${sectionName}"]`);
    if (activeNavItem) {
        activeNavItem.classList.add('active');
    }
    
    // Cargar datos específicos de la sección
    cargarDatosSeccion(sectionName);
}

// Cargar datos específicos según la sección
function cargarDatosSeccion(sectionName) {
    switch(sectionName) {
        case 'dashboard':
            cargarEstadisticas();
            break;
        case 'clientes':
            cargarClientes();
            break;
        case 'errores':
            cargarErrores();
            break;
        case 'estadisticas':
            cargarEstadisticasDetalladas();
            break;
    }
}

// Configurar formularios
function configurarFormularios() {
    // Formulario de nuevo cliente
    const formCliente = document.getElementById('form-nuevo-cliente');
    if (formCliente) {
        formCliente.addEventListener('submit', function(e) {
            e.preventDefault();
            guardarCliente();
        });
    }
}

// Cargar datos iniciales
function cargarDatosIniciales() {
    cargarEstadisticas();
    cargarClientes();
}

// === FUNCIONES DE API ===

// Función genérica para hacer llamadas a la API
async function apiCall(endpoint, method = 'GET', data = null) {
    try {
        const options = {
            method: method,
            headers: {
                'Content-Type': 'application/json',
            }
        };
        
        if (data) {
            options.body = JSON.stringify(data);
        }
        
        const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error(`Error en API call ${endpoint}:`, error);
        mostrarToast(`Error: ${error.message}`, 'error');
        throw error;
    }
}

// === FUNCIONES DE CLIENTES ===

// Cargar lista de clientes
async function cargarClientes() {
    try {
        mostrarLoading(true);
        const clientes = await apiCall('/clientes');
        clientesData = clientes;
        mostrarClientes(clientes);
        actualizarContadorClientes(clientes.length);
    } catch (error) {
        console.error('Error cargando clientes:', error);
        mostrarError('Error al cargar clientes');
    } finally {
        mostrarLoading(false);
    }
}

// Mostrar clientes en la tabla
function mostrarClientes(clientes) {
    const tbody = document.getElementById('clientes-tbody');
    
    if (!clientes || clientes.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center">No hay clientes registrados</td></tr>';
        return;
    }
    
    tbody.innerHTML = clientes.map(cliente => `
        <tr>
            <td>${cliente.idCliente}</td>
            <td>${cliente.nombreCliente}</td>
            <td>${cliente.apellidoCliente}</td>
            <td>${cliente.email}</td>
            <td>${cliente.telefono}</td>
            <td>${cliente.razonSocial || 'N/A'}</td>
            <td>
                <button class="btn btn-info action-btn" onclick="editarCliente(${cliente.idCliente})">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn btn-danger action-btn" onclick="eliminarCliente(${cliente.idCliente})">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

// Mostrar formulario de cliente
function mostrarFormularioCliente() {
    const form = document.getElementById('cliente-form');
    form.style.display = 'block';
    form.scrollIntoView({ behavior: 'smooth' });
}

// Cancelar formulario de cliente
function cancelarFormulario() {
    document.getElementById('cliente-form').style.display = 'none';
    document.getElementById('form-nuevo-cliente').reset();
}

// Guardar cliente
async function guardarCliente() {
    try {
        const form = document.getElementById('form-nuevo-cliente');
        const formData = new FormData(form);
        
        const cliente = {
            nombreCliente: formData.get('nombre'),
            apellidoCliente: formData.get('apellido'),
            email: formData.get('email'),
            telefono: formData.get('telefono'),
            razonSocial: formData.get('razonSocial') || null
        };
        
        mostrarLoading(true);
        await apiCall('/clientes', 'POST', cliente);
        
        mostrarToast('Cliente guardado exitosamente', 'success');
        cancelarFormulario();
        cargarClientes();
        
    } catch (error) {
        console.error('Error guardando cliente:', error);
        mostrarToast('Error al guardar cliente', 'error');
    } finally {
        mostrarLoading(false);
    }
}

// Eliminar cliente
async function eliminarCliente(id) {
    if (!confirm('¿Está seguro de que desea eliminar este cliente?')) {
        return;
    }
    
    try {
        mostrarLoading(true);
        await apiCall(`/clientes/${id}`, 'DELETE');
        
        mostrarToast('Cliente eliminado exitosamente', 'success');
        cargarClientes();
        
    } catch (error) {
        console.error('Error eliminando cliente:', error);
        mostrarToast('Error al eliminar cliente', 'error');
    } finally {
        mostrarLoading(false);
    }
}

// === FUNCIONES DE ESTADÍSTICAS ===

// Cargar estadísticas
async function cargarEstadisticas() {
    try {
        const stats = await apiCall('/stats');
        estadisticasData = stats;
        actualizarDashboard(stats);
        actualizarEstadisticasHeader(stats);
    } catch (error) {
        console.error('Error cargando estadísticas:', error);
        mostrarError('Error al cargar estadísticas');
    }
}

// Actualizar dashboard con estadísticas
function actualizarDashboard(stats) {
    // Actualizar contadores principales
    document.getElementById('dash-total-clientes').textContent = stats.totalClientes || 0;
    document.getElementById('dash-errores-recientes').textContent = stats.erroresRecientes || 0;
    document.getElementById('uptime').textContent = stats.tiempoActividad || '0s';
    
    // Actualizar información de memoria
    if (stats.memoria) {
        const porcentaje = stats.memoria.porcentajeUso || 0;
        document.getElementById('memory-usage-bar').style.width = `${porcentaje}%`;
        document.getElementById('memory-used').textContent = stats.memoria.usada || '0 MB';
        document.getElementById('memory-total').textContent = stats.memoria.total || '0 MB';
    }
}

// Actualizar estadísticas en el header
function actualizarEstadisticasHeader(stats = null) {
    if (!stats) {
        // Si no se pasan stats, cargar desde la API
        cargarEstadisticas();
        return;
    }
    
    document.getElementById('total-clientes').textContent = stats.totalClientes || 0;
    document.getElementById('total-errores').textContent = stats.erroresRecientes || 0;
    document.getElementById('estado-servidor').textContent = 'Online';
}

// Actualizar contador de clientes
function actualizarContadorClientes(count) {
    document.getElementById('total-clientes').textContent = count;
    document.getElementById('dash-total-clientes').textContent = count;
}

// Cargar estadísticas detalladas
function cargarEstadisticasDetalladas() {
    const statsContent = document.getElementById('stats-content');
    
    if (!estadisticasData || Object.keys(estadisticasData).length === 0) {
        statsContent.innerHTML = '<div class="loading">Cargando estadísticas...</div>';
        cargarEstadisticas().then(() => {
            mostrarEstadisticasDetalladas();
        });
        return;
    }
    
    mostrarEstadisticasDetalladas();
}

// Mostrar estadísticas detalladas
function mostrarEstadisticasDetalladas() {
    const statsContent = document.getElementById('stats-content');
    const stats = estadisticasData;
    
    statsContent.innerHTML = `
        <div class="dashboard-grid">
            <div class="dashboard-card">
                <div class="card-header">
                    <h3><i class="fas fa-users"></i> Clientes</h3>
                </div>
                <div class="card-content">
                    <div class="big-number">${stats.totalClientes || 0}</div>
                    <p>Total de clientes registrados</p>
                </div>
            </div>
            
            <div class="dashboard-card">
                <div class="card-header">
                    <h3><i class="fas fa-database"></i> Base de Datos</h3>
                </div>
                <div class="card-content">
                    <div class="big-number">${stats.poolConexiones || 0}</div>
                    <p>Conexiones en el pool</p>
                </div>
            </div>
            
            <div class="dashboard-card">
                <div class="card-header">
                    <h3><i class="fas fa-exclamation-triangle"></i> Errores</h3>
                </div>
                <div class="card-content">
                    <div class="big-number error-count">${stats.erroresRecientes || 0}</div>
                    <p>Errores en las últimas 24h</p>
                </div>
            </div>
            
            <div class="dashboard-card">
                <div class="card-header">
                    <h3><i class="fas fa-clock"></i> Tiempo Activo</h3>
                </div>
                <div class="card-content">
                    <div class="big-number">${stats.tiempoActividad || '0s'}</div>
                    <p>Tiempo de actividad del sistema</p>
                </div>
            </div>
        </div>
        
        ${stats.memoria ? `
        <div class="dashboard-card" style="margin-top: 2rem;">
            <div class="card-header">
                <h3><i class="fas fa-memory"></i> Uso de Memoria Detallado</h3>
            </div>
            <div class="card-content">
                <div class="memory-info">
                    <p><strong>Total:</strong> ${stats.memoria.total}</p>
                    <p><strong>Usada:</strong> ${stats.memoria.usada}</p>
                    <p><strong>Libre:</strong> ${stats.memoria.libre}</p>
                    <p><strong>Porcentaje de uso:</strong> ${stats.memoria.porcentajeUso}%</p>
                    <div class="memory-bar" style="margin-top: 1rem;">
                        <div class="memory-usage" style="width: ${stats.memoria.porcentajeUso}%;"></div>
                    </div>
                </div>
            </div>
        </div>
        ` : ''}
    `;
}

// === FUNCIONES DE ERRORES ===

// Cargar errores
async function cargarErrores() {
    try {
        mostrarLoading(true);
        const errores = await apiCall('/errores');
        mostrarErrores(errores);
    } catch (error) {
        console.error('Error cargando errores:', error);
        mostrarError('Error al cargar log de errores');
    } finally {
        mostrarLoading(false);
    }
}

// Mostrar errores en la tabla
function mostrarErrores(errores) {
    const tbody = document.getElementById('errores-tbody');
    
    if (!errores || errores.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center">No hay errores registrados</td></tr>';
        return;
    }
    
    tbody.innerHTML = errores.map(error => `
        <tr>
            <td>${error.idError}</td>
            <td><span class="badge badge-danger">${error.tipoError || 'General'}</span></td>
            <td>${error.descripcion}</td>
            <td>${error.operacion || 'N/A'}</td>
            <td>${error.usuarioAfectado || 'N/A'}</td>
            <td>${formatearFecha(error.fechaError)}</td>
        </tr>
    `).join('');
}

// === FUNCIONES DE CONCURRENCIA ===

// Ejecutar prueba de concurrencia
async function ejecutarPruebaConcurrencia(tipoTest) {
    try {
        mostrarLoading(true);
        const resultado = await apiCall(`/concurrency/${tipoTest}`, 'POST');
        
        mostrarResultadoConcurrencia(resultado);
        mostrarToast('Prueba de concurrencia iniciada', 'success');
        
    } catch (error) {
        console.error('Error ejecutando prueba de concurrencia:', error);
        mostrarToast('Error al ejecutar prueba de concurrencia', 'error');
    } finally {
        mostrarLoading(false);
    }
}

// Mostrar resultado de prueba de concurrencia
function mostrarResultadoConcurrencia(resultado) {
    const contenedor = document.getElementById('resultado-concurrencia');
    const contenido = document.getElementById('concurrencia-contenido');
    
    contenido.innerHTML = `
        <div class="resultado-item">
            <h4><i class="fas fa-cogs"></i> ${resultado.prueba}</h4>
            <p><strong>Estado:</strong> <span class="text-info">${resultado.estado}</span></p>
            <p><strong>Mensaje:</strong> ${resultado.mensaje}</p>
            
            ${resultado.hilos ? `<p><strong>Hilos utilizados:</strong> ${resultado.hilos}</p>` : ''}
            ${resultado.operacion ? `<p><strong>Operación:</strong> ${resultado.operacion}</p>` : ''}
            ${resultado.operaciones ? `<p><strong>Operaciones:</strong> ${resultado.operaciones.join(', ')}</p>` : ''}
            ${resultado.propiedades ? `<p><strong>Propiedades ACID:</strong> ${resultado.propiedades.join(', ')}</p>` : ''}
            ${resultado.escenarios ? `<p><strong>Escenarios probados:</strong> ${resultado.escenarios.join(', ')}</p>` : ''}
            
            <div class="alert alert-info" style="margin-top: 1rem;">
                <i class="fas fa-info-circle"></i>
                La prueba se está ejecutando en segundo plano. Consulte los logs del servidor para ver los resultados detallados.
            </div>
        </div>
    `;
    
    contenedor.style.display = 'block';
    contenedor.scrollIntoView({ behavior: 'smooth' });
}

// === FUNCIONES DE TRANSACCIONES ACID ===

// Simular transacción atómica
async function simularTransaccionAtomica() {
    mostrarResultadoTransaccion({
        tipo: 'Atomicidad',
        descripcion: 'Simulando rollback de transacción...',
        resultado: 'Se insertarán 3 clientes en una transacción. Si falla el tercero, se revierten todos los cambios.',
        icono: 'fas fa-atom',
        color: 'info'
    });
    
    // Aquí podrías hacer una llamada real a la API para simular la transacción
    mostrarToast('Simulación de atomicidad iniciada', 'info');
}

// Simular consistencia
async function simularConsistencia() {
    mostrarResultadoTransaccion({
        tipo: 'Consistencia',
        descripcion: 'Verificando integridad de datos...',
        resultado: 'Se verifican las restricciones de integridad referencial y las reglas de negocio.',
        icono: 'fas fa-balance-scale',
        color: 'success'
    });
    
    mostrarToast('Verificación de consistencia iniciada', 'info');
}

// Simular aislamiento
async function simularAislamiento() {
    mostrarResultadoTransaccion({
        tipo: 'Aislamiento',
        descripcion: 'Probando transacciones concurrentes...',
        resultado: 'Se ejecutan múltiples transacciones simultáneas para verificar que no interfieren entre sí.',
        icono: 'fas fa-shield-alt',
        color: 'warning'
    });
    
    mostrarToast('Prueba de aislamiento iniciada', 'info');
}

// Simular durabilidad
async function simularDurabilidad() {
    mostrarResultadoTransaccion({
        tipo: 'Durabilidad',
        descripcion: 'Verificando persistencia de datos...',
        resultado: 'Se confirma que los cambios persisten después del commit, incluso ante fallos del sistema.',
        icono: 'fas fa-save',
        color: 'primary'
    });
    
    mostrarToast('Verificación de durabilidad iniciada', 'info');
}

// Mostrar resultado de transacción
function mostrarResultadoTransaccion(datos) {
    const contenedor = document.getElementById('resultado-transaccion');
    const contenido = document.getElementById('resultado-contenido');
    
    contenido.innerHTML = `
        <div class="resultado-item">
            <h4><i class="${datos.icono}"></i> ${datos.tipo}</h4>
            <p><strong>Descripción:</strong> ${datos.descripcion}</p>
            <div class="alert alert-${datos.color}">
                <i class="fas fa-info-circle"></i>
                ${datos.resultado}
            </div>
        </div>
    `;
    
    contenedor.style.display = 'block';
    contenedor.scrollIntoView({ behavior: 'smooth' });
}

// === FUNCIONES DE UI ===

// Mostrar/ocultar loading overlay
function mostrarLoading(mostrar) {
    const overlay = document.getElementById('loading-overlay');
    overlay.style.display = mostrar ? 'flex' : 'none';
}

// Mostrar toast notification
function mostrarToast(mensaje, tipo = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${tipo}`;
    
    const iconos = {
        success: 'fas fa-check-circle',
        error: 'fas fa-exclamation-circle',
        warning: 'fas fa-exclamation-triangle',
        info: 'fas fa-info-circle'
    };
    
    toast.innerHTML = `
        <i class="${iconos[tipo] || iconos.info}"></i>
        <span>${mensaje}</span>
    `;
    
    container.appendChild(toast);
    
    // Remover el toast después de 5 segundos
    setTimeout(() => {
        if (toast.parentNode) {
            toast.parentNode.removeChild(toast);
        }
    }, 5000);
}

// Mostrar mensaje de error
function mostrarError(mensaje) {
    mostrarToast(mensaje, 'error');
}

// === FUNCIONES AUXILIARES ===

// Formatear fecha
function formatearFecha(fechaString) {
    if (!fechaString) return 'N/A';
    
    const fecha = new Date(fechaString);
    return fecha.toLocaleString('es-ES', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Buscar clientes (función para el buscador)
function buscarClientes() {
    const termino = document.getElementById('buscar-cliente').value.toLowerCase();
    
    if (!termino) {
        mostrarClientes(clientesData);
        return;
    }
    
    const clientesFiltrados = clientesData.filter(cliente => 
        cliente.nombreCliente.toLowerCase().includes(termino) ||
        cliente.apellidoCliente.toLowerCase().includes(termino) ||
        cliente.email.toLowerCase().includes(termino)
    );
    
    mostrarClientes(clientesFiltrados);
}

// Configurar búsqueda en tiempo real
document.addEventListener('DOMContentLoaded', function() {
    const buscador = document.getElementById('buscar-cliente');
    if (buscador) {
        buscador.addEventListener('input', buscarClientes);
    }
});

// === FUNCIONES ADICIONALES ===

// Editar cliente (placeholder)
function editarCliente(id) {
    mostrarToast('Función de edición en desarrollo', 'info');
    console.log('Editando cliente:', id);
}

// Función para manejar errores de red
window.addEventListener('online', function() {
    mostrarToast('Conexión restaurada', 'success');
    document.getElementById('estado-servidor').textContent = 'Online';
});

window.addEventListener('offline', function() {
    mostrarToast('Sin conexión a internet', 'error');
    document.getElementById('estado-servidor').textContent = 'Offline';
});

// Log para debugging
console.log('🎨 Frontend JavaScript cargado correctamente'); 