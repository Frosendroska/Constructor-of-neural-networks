import * as api from 'nnbuilder-api'

export const authService = new api.AuthServicePromiseClient(process.env.API_HOST!, null)
export const modificationService = new api.NNModificationServicePromiseClient(process.env.API_HOST!, null)
