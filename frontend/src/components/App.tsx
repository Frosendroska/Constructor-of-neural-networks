import React, {useState} from 'react'
import * as api from '../clients'

export default function App(): JSX.Element {
    const [sum, setSum] = useState<number | undefined>(undefined)
    const [lhs, setLhs] = useState<number | undefined>(undefined)
    const [rhs, setRhs] = useState<number | undefined>(undefined)

    return <div>
        <h1>Sum Calculator</h1>
        <form onSubmit={(event) => {
            event.preventDefault()

            if (lhs === undefined || rhs === undefined) return

            const request = new api.GetSumRequest()
                .setLhs(lhs)
                .setRhs(rhs)
            api.sumService.getSum(request).then((value: api.GetSumResponse) => {
                setSum(value.getSum())
            });;;;
        }}>
            <label>
                lhs:
                <input
                    type="number"
                    value={lhs || ''}
                    onChange={(event) => setLhs(Number(event.target.value))}
                />
            </label>
            <br />
            <label>
                rhs:
                <input
                    type="number"
                    value={rhs || ''}
                    onChange={(event) => setRhs(Number(event.target.value))}
                />
            </label>
            <br />
            <input type="submit" value="Compute sum" />
        </form>
        <h2>Computed sum: {sum}</h2>
    </div>
}
