import * as React from 'react';
import { ReactElement } from 'react';
import { render as reactRender } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom'
import { RecoilRoot } from 'recoil';
import ThemeProvider from 'styles/ThemeProvider'
// import reportWebVitals from '../reportWebVitals'

const render = (ui: ReactElement, { ...options } = {}) =>
    reactRender(ui, {
        wrapper: ({ children }) => (
            <React.StrictMode>
                <BrowserRouter>
                    <RecoilRoot>
                        <ThemeProvider>
                            { children }
                        </ThemeProvider>
                    </RecoilRoot>
                </BrowserRouter>
            </React.StrictMode>
        ),
        ...options
    });

export * from '@testing-library/react';
export { render };