<template>
    <div class="main-container">
        <div class="main-content">
            <slot></slot>
        </div>
    </div>
</template>

<script>
    export default {
        name: "PageBody"
    }
</script>

<style scoped>
    .main-container {
        padding-top: 60px;
        display: flex;
        justify-content: center;
        background-color: #f6f6f6;
        min-height: 100vh;
    }

    .main-content {
        width: 1000px;
        min-height: 90vh;
        margin: 0;
        border-radius: 8px;
        position: relative;
        overflow: hidden;
        background: transparent;
        display: flex;
        flex-direction: column;
    }
</style>