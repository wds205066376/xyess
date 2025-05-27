<template>
    <div class="foot-container">
        <div class="author">© 2025 校园二手书交易系统</div>
    </div>
</template>

<script>
    export default {
        name: "Foot"
    }
</script>

<style scoped>
    .foot-container {
        width: 100%;
        display: flex;
        justify-content: center;
        height: 40px;
        padding-top: 10px;
        padding-bottom: 20px;
        background: transparent;
        position: relative;
        margin-top: auto;
        flex-shrink: 0;
    }

    .author {
        color: #999999;
        font-size: 14px;
    }
</style>
