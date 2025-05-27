import { defineStore } from 'pinia'

export const useGlobalStore = defineStore('global', {
  state: () => ({
    baseApi: 'http://localhost:9321'
  })
}) 